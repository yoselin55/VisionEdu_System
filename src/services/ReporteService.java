package services;

import com.itextpdf.text.*;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import models.Asistencia;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReporteService {

    public List<Asistencia> filtrarPorAnio(List<Asistencia> lista, int anio) {
        return lista.stream()
                .filter(a -> a.getFecha() != null &&
                        a.getFecha().toLocalDate().getYear() == anio)
                .collect(Collectors.toList());
    }

    public List<Asistencia> filtrarPorMes(List<Asistencia> lista, int anio, int mes) {
        return lista.stream()
                .filter(a -> a.getFecha() != null &&
                        a.getFecha().toLocalDate().getYear() == anio &&
                        a.getFecha().toLocalDate().getMonthValue() == mes)
                .collect(Collectors.toList());
    }

    public List<Asistencia> filtrarPorSemana(List<Asistencia> lista, LocalDate inicioSemana) {
        LocalDate fin = inicioSemana.plusDays(6);
        return lista.stream()
                .filter(a -> a.getFecha() != null)
                .filter(a -> {
                    LocalDate f = a.getFecha().toLocalDate();
                    return !f.isBefore(inicioSemana) && !f.isAfter(fin);
                })
                .collect(Collectors.toList());
    }

    private Map<String, String> analizarEstadisticas(List<Asistencia> lista) {
        if (lista.isEmpty()) return Map.of(
                "riesgo", "N/A", "estrella", "N/A",
                "faltasTotal", "0", "tasaPuntualidad", "0%");

        String masTardanzas = lista.stream()
                .filter(a -> "Tarde".equalsIgnoreCase(a.getEstado()))
                .collect(Collectors.groupingBy(Asistencia::getNombreAlumno, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .orElse("Ninguno");

        String masPuntual = lista.stream()
                .filter(a -> "Presente".equalsIgnoreCase(a.getEstado()))
                .collect(Collectors.groupingBy(Asistencia::getNombreAlumno, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .orElse("Ninguno");

        long total     = lista.size();
        long presentes = lista.stream()
                .filter(a -> "Presente".equalsIgnoreCase(a.getEstado())).count();
        double tasa = total > 0 ? (double) presentes / total * 100 : 0;

        return Map.of(
                "riesgo",          masTardanzas,
                "estrella",        masPuntual,
                "faltasTotal",     String.valueOf(lista.stream()
                        .filter(a -> "Falta".equalsIgnoreCase(a.getEstado())).count()),
                "tasaPuntualidad", String.format("%.1f%%", tasa)
        );
    }

    public void exportarExcel(List<Asistencia> lista, String ruta) throws Exception {
        exportarExcelConPeriodo(lista, ruta, "Reporte Completo");
    }

    public void exportarExcelConPeriodo(List<Asistencia> lista,
                                        String ruta,
                                        String tituloPeriodo) throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Asistencia");
            Map<String, String> stats = analizarEstadisticas(lista);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // ── Estilos ──────────────────────────────────────────────────────
            CellStyle titleStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font tf = wb.createFont();
            tf.setBold(true); tf.setFontHeightInPoints((short) 16);
            tf.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFont(tf);

            CellStyle periodoStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font pf = wb.createFont();
            pf.setBold(true); pf.setFontHeightInPoints((short) 12);
            pf.setColor(IndexedColors.DARK_TEAL.getIndex());
            periodoStyle.setFont(pf);

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorders(headerStyle, BorderStyle.THIN);
            org.apache.poi.ss.usermodel.Font hf = wb.createFont();
            hf.setBold(true); hf.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(hf);

            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorders(dataStyle, BorderStyle.THIN);

            CellStyle presenteStyle = wb.createCellStyle();
            presenteStyle.cloneStyleFrom(dataStyle);
            presenteStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            presenteStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font gf = wb.createFont();
            gf.setBold(true); gf.setColor(IndexedColors.DARK_GREEN.getIndex());
            presenteStyle.setFont(gf);

            CellStyle tardeStyle = wb.createCellStyle();
            tardeStyle.cloneStyleFrom(dataStyle);
            tardeStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            tardeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font yf = wb.createFont();
            yf.setBold(true); yf.setColor(IndexedColors.DARK_YELLOW.getIndex());
            tardeStyle.setFont(yf);

            CellStyle faltaStyle = wb.createCellStyle();
            faltaStyle.cloneStyleFrom(dataStyle);
            faltaStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            faltaStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font rf = wb.createFont();
            rf.setBold(true); rf.setColor(IndexedColors.DARK_RED.getIndex());
            faltaStyle.setFont(rf);

            CellStyle statStyle = wb.createCellStyle();
            statStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            statStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorders(statStyle, BorderStyle.THIN);
            statStyle.setAlignment(HorizontalAlignment.CENTER);

            // ── Encabezado del reporte ────────────────────────────────────────
            Row r0 = sheet.createRow(0);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("VISIONEDU - REPORTE DE ASISTENCIA");
            c0.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            Row r1 = sheet.createRow(1);
            Cell c1 = r1.createCell(0);
            c1.setCellValue("Periodo: " + tituloPeriodo);
            c1.setCellStyle(periodoStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Generado: " + LocalDateTime.now().format(dtf));

            // ── Fila de estadísticas ──────────────────────────────────────────
            Row r4 = sheet.createRow(4);
            String[] statLabels = {"Total", "Presentes", "Tardanzas", "Faltas",
                    "Puntualidad", "Alumno estrella"};
            long total     = lista.size();
            long presentes = lista.stream().filter(a -> "Presente".equalsIgnoreCase(a.getEstado())).count();
            long tardes    = lista.stream().filter(a -> "Tarde".equalsIgnoreCase(a.getEstado())).count();
            long faltas    = lista.stream().filter(a -> "Falta".equalsIgnoreCase(a.getEstado())).count();
            String[] statVals = {
                    String.valueOf(total), String.valueOf(presentes),
                    String.valueOf(tardes), String.valueOf(faltas),
                    stats.get("tasaPuntualidad"), stats.get("estrella")
            };
            for (int i = 0; i < statLabels.length; i++) {
                Cell lbl = r4.createCell(i);
                lbl.setCellValue(statLabels[i]);
                lbl.setCellStyle(headerStyle);
            }
            Row r5 = sheet.createRow(5);
            for (int i = 0; i < statVals.length; i++) {
                Cell val = r5.createCell(i);
                val.setCellValue(statVals[i]);
                val.setCellStyle(statStyle);
            }

            // ── Cabecera de tabla — ahora con GRADO ───────────────────────────
            String[] headers = {"ID", "ALUMNO", "GRADO", "DÍA", "FECHA", "HORA", "ESTADO"};
            Row hr = sheet.createRow(7);
            for (int i = 0; i < headers.length; i++) {
                Cell c = hr.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // ── Datos agrupados por alumno ────────────────────────────────────
            Map<String, List<Asistencia>> porAlumno = lista.stream()
                    .collect(Collectors.groupingBy(a ->
                            a.getNombreAlumno() != null ? a.getNombreAlumno() : ""));

            int rowNum = 8;
            for (Map.Entry<String, List<Asistencia>> entry : porAlumno.entrySet()) {
                List<Asistencia> asists = entry.getValue().stream()
                        .sorted((a, b) -> {
                            if (a.getFecha() == null) return 1;
                            if (b.getFecha() == null) return -1;
                            return a.getFecha().compareTo(b.getFecha());
                        })
                        .collect(Collectors.toList());

                for (Asistencia a : asists) {
                    Row row = sheet.createRow(rowNum++);

                    setCellData(row, 0, String.valueOf(a.getIdAlumno()), dataStyle);
                    setCellData(row, 1, a.getNombreAlumno() != null ? a.getNombreAlumno() : "—", dataStyle);
                    // ← columna GRADO nueva
                    setCellData(row, 2, a.getGrado() != null ? a.getGrado() : "—", dataStyle);

                    String dia = "—";
                    if (a.getFecha() != null) {
                        dia = switch (a.getFecha().toLocalDate().getDayOfWeek()) {
                            case MONDAY    -> "Lunes";
                            case TUESDAY   -> "Martes";
                            case WEDNESDAY -> "Miércoles";
                            case THURSDAY  -> "Jueves";
                            case FRIDAY    -> "Viernes";
                            case SATURDAY  -> "Sábado";
                            case SUNDAY    -> "Domingo";
                        };
                    }
                    setCellData(row, 3, dia, dataStyle);
                    setCellData(row, 4, a.getFecha() != null ? a.getFecha().toString() : "—", dataStyle);

                    String hora = "—";
                    if (a.getHora() != null) {
                        String hs = a.getHora().toString();
                        hora = hs.length() >= 5 ? hs.substring(0, 5) : hs;
                    }
                    setCellData(row, 5, hora, dataStyle);

                    // Estado con color
                    Cell cEst = row.createCell(6);
                    cEst.setCellValue(a.getEstado() != null ? a.getEstado() : "—");
                    if      ("Presente".equalsIgnoreCase(a.getEstado())) cEst.setCellStyle(presenteStyle);
                    else if ("Tarde".equalsIgnoreCase(a.getEstado()))    cEst.setCellStyle(tardeStyle);
                    else if ("Falta".equalsIgnoreCase(a.getEstado()))    cEst.setCellStyle(faltaStyle);
                    else                                                  cEst.setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream out = new FileOutputStream(ruta)) {
                wb.write(out);
            }
            System.out.println("Excel exportado: " + ruta);
        }
    }

    private void setCellData(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private void setBorders(CellStyle style, BorderStyle bs) {
        style.setBorderTop(bs); style.setBorderBottom(bs);
        style.setBorderLeft(bs); style.setBorderRight(bs);
    }

    // ── PDF ──────────────────────────────────────────────────────────────────

    public void exportarPDF(List<Asistencia> lista, String ruta) throws Exception {
        exportarPDFConPeriodo(lista, ruta, "Reporte Completo");
    }

    public void exportarPDFConPeriodo(List<Asistencia> lista,
                                      String ruta,
                                      String tituloPeriodo) throws Exception {
        Document doc = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(doc, new FileOutputStream(ruta));
        doc.open();

        Map<String, String> stats = analizarEstadisticas(lista);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        Font fTit  = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD,  new BaseColor(26, 35, 126));
        Font fPer  = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD,  new BaseColor(21, 101, 192));
        Font fSub  = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
        Font fHead = new Font(Font.FontFamily.HELVETICA, 9,  Font.BOLD,  BaseColor.WHITE);
        Font fBody = new Font(Font.FontFamily.HELVETICA, 8);
        Font fBold = new Font(Font.FontFamily.HELVETICA, 8,  Font.BOLD);

        doc.add(new Paragraph("VISIONEDU - SISTEMA DE ASISTENCIA", fTit));
        doc.add(new Paragraph("Periodo: " + tituloPeriodo, fPer));
        doc.add(new Paragraph("Generado el: " + LocalDateTime.now().format(dtf), fSub));
        doc.add(new Paragraph(" "));

        // Resumen estadístico
        PdfPTable resumen = new PdfPTable(4);
        resumen.setWidthPercentage(100);
        long total     = lista.size();
        long presentes = lista.stream().filter(a -> "Presente".equalsIgnoreCase(a.getEstado())).count();
        long tardes    = lista.stream().filter(a -> "Tarde".equalsIgnoreCase(a.getEstado())).count();
        long faltas    = lista.stream().filter(a -> "Falta".equalsIgnoreCase(a.getEstado())).count();
        resumen.addCell(statCell("Total: "     + total,     new BaseColor(200, 210, 240)));
        resumen.addCell(statCell("Presentes: " + presentes, new BaseColor(200, 230, 201)));
        resumen.addCell(statCell("Tardanzas: " + tardes,    new BaseColor(255, 236, 179)));
        resumen.addCell(statCell("Faltas: "    + faltas,    new BaseColor(255, 205, 210)));
        doc.add(resumen);
        doc.add(new Paragraph("Puntualidad: " + stats.get("tasaPuntualidad") +
                "   |   Alumno estrella: " + stats.get("estrella"), fSub));
        doc.add(new Paragraph(" "));

        // ── Tabla datos — ahora con GRADO ────────────────────────────────────
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        // ← ancho relativo de cada columna, GRADO agregado
        table.setWidths(new float[]{0.6f, 2.8f, 1.5f, 1.0f, 1.3f, 1.0f, 1.3f});

        String[] headers = {"ID", "ALUMNO", "GRADO", "DÍA", "FECHA", "HORA", "ESTADO"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fHead));
            cell.setBackgroundColor(new BaseColor(26, 35, 126));
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        List<Asistencia> ordenada = lista.stream()
                .sorted((a, b) -> {
                    if (a.getFecha() == null) return 1;
                    if (b.getFecha() == null) return -1;
                    int cmp = a.getFecha().compareTo(b.getFecha());
                    if (cmp != 0) return cmp;
                    return a.getNombreAlumno() != null
                            ? a.getNombreAlumno().compareTo(
                            b.getNombreAlumno() != null ? b.getNombreAlumno() : "")
                            : 0;
                })
                .collect(Collectors.toList());

        boolean altRow = false;
        for (Asistencia a : ordenada) {
            BaseColor rowBg = altRow ? new BaseColor(245, 245, 250) : BaseColor.WHITE;
            altRow = !altRow;

            String dia = "—";
            if (a.getFecha() != null) {
                dia = switch (a.getFecha().toLocalDate().getDayOfWeek()) {
                    case MONDAY    -> "Lunes";
                    case TUESDAY   -> "Martes";
                    case WEDNESDAY -> "Miércoles";
                    case THURSDAY  -> "Jueves";
                    case FRIDAY    -> "Viernes";
                    case SATURDAY  -> "Sábado";
                    case SUNDAY    -> "Domingo";
                };
            }
            String hora = "—";
            if (a.getHora() != null) {
                String hs = a.getHora().toString();
                hora = hs.length() >= 5 ? hs.substring(0, 5) : hs;
            }

            addPdfCell(table, String.valueOf(a.getIdAlumno()),                          fBody, rowBg, Element.ALIGN_CENTER);
            addPdfCell(table, a.getNombreAlumno() != null ? a.getNombreAlumno() : "—", fBold, rowBg, Element.ALIGN_LEFT);
            // ← celda GRADO nueva
            addPdfCell(table, a.getGrado() != null ? a.getGrado() : "—",              fBody, rowBg, Element.ALIGN_CENTER);
            addPdfCell(table, dia,                                                      fBody, rowBg, Element.ALIGN_CENTER);
            addPdfCell(table, a.getFecha() != null ? a.getFecha().toString() : "—",   fBody, rowBg, Element.ALIGN_CENTER);
            addPdfCell(table, hora,                                                     fBody, rowBg, Element.ALIGN_CENTER);

            BaseColor estColor = switch (a.getEstado() != null ? a.getEstado().toLowerCase() : "") {
                case "presente" -> new BaseColor(200, 230, 201);
                case "tarde"    -> new BaseColor(255, 236, 179);
                case "falta"    -> new BaseColor(255, 205, 210);
                default         -> rowBg;
            };
            Font estFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
            addPdfCell(table, a.getEstado() != null ? a.getEstado().toUpperCase() : "—",
                    estFont, estColor, Element.ALIGN_CENTER);
        }

        doc.add(table);
        doc.close();
        System.out.println("PDF exportado: " + ruta);
    }

    private PdfPCell statCell(String text, BaseColor color) {
        PdfPCell cell = new PdfPCell(
                new Phrase(text, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
        cell.setBackgroundColor(color);
        cell.setPadding(7);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private void addPdfCell(PdfPTable table, String text, Font font,
                            BaseColor bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(4);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }
}