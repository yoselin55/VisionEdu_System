package data;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_face.*;

import java.io.File;
import java.nio.IntBuffer;
import java.util.List;

public class FaceRecognition {

    private static final String CASCADE_PATH       = "src/resources/cascades/haarcascade_frontalface_alt.xml";
    private static final String TRAINING_DATA_PATH = "src/data/images/";

    private static final double UMBRAL_RECONOCIMIENTO = 120.0;

    private CascadeClassifier   faceDetector;
    private LBPHFaceRecognizer  faceRecognizer;
    private boolean modeloCargado = false;
    private boolean modoOscuro    = true;

    private int    frameCounter     = 0;
    private double animationProgress = 0.0;

    public FaceRecognition() {
        faceDetector  = new CascadeClassifier(CASCADE_PATH);
        faceRecognizer = LBPHFaceRecognizer.create();
        File dir = new File(TRAINING_DATA_PATH);
        if (!dir.exists()) dir.mkdirs();
        cargarModelo();
    }

    public void setModoOscuro(boolean oscuro) { this.modoOscuro = oscuro; }

    private void cargarModelo() {
        File modelFile = new File(TRAINING_DATA_PATH + "trainedModel.yml");
        if (modelFile.exists()) {
            try {
                faceRecognizer.read(TRAINING_DATA_PATH + "trainedModel.yml");
                modeloCargado = true;
                System.out.println("Modelo cargado");
            } catch (Exception e) {
                System.err.println("Error modelo: " + e.getMessage());
                modeloCargado = false;
            }
        } else {
            modeloCargado = false;
        }
    }

    public Mat detectFace(Mat frame) {
        if (frame == null || frame.empty()) return frame;
        RectVector faces = new RectVector();
        Mat gray = new Mat();
        cvtColor(frame, gray, COLOR_BGR2GRAY);
        faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0,
                new Size(100, 100), new Size());

        if (faces.size() > 0) {
            frameCounter++;
            animationProgress = Math.min(1.0, frameCounter / 30.0);
        } else {
            frameCounter      = 0;
            animationProgress = 0.0;
        }
        for (int i = 0; i < faces.size(); i++)
            dibujarMallaProfesional(frame, faces.get(i), animationProgress);
        return frame;
    }

    private void dibujarMallaProfesional(Mat frame, Rect rect, double progress) {
        int x = rect.x(), y = rect.y(), w = rect.width(), h = rect.height();

        Scalar cVerde  = modoOscuro ? new Scalar(0, 230, 80, 0)    : new Scalar(0, 150, 50, 0);
        Scalar cCyan   = modoOscuro ? new Scalar(220, 200, 0, 0)   : new Scalar(150, 100, 0, 0);
        Scalar cBlanco = modoOscuro ? new Scalar(200, 200, 200, 0) : new Scalar(40, 40, 40, 0);
        Scalar cMalla  = modoOscuro ? new Scalar(0, 180, 60, 0)    : new Scalar(0, 120, 40, 0);
        Scalar cOscuro = modoOscuro ? new Scalar(0, 60, 20, 0)     : new Scalar(180, 220, 180, 0);

        if (progress > 0.15) {
            Mat ov = frame.clone();
            rectangle(new Mat(ov, rect), new Point(0,0), new Point(w,h),
                    cOscuro, FILLED, LINE_8, 0);
            addWeighted(new Mat(frame, rect), 0.88,
                    new Mat(ov, rect), 0.12, 0, new Mat(frame, rect));
        }

        int cl = (int)(w / 6.0 * Math.min(progress * 4, 1.0));
        int th = 2;
        line(frame, new Point(x,   y),   new Point(x+cl, y),   cVerde, th, LINE_8, 0);
        line(frame, new Point(x,   y),   new Point(x,   y+cl), cVerde, th, LINE_8, 0);
        if (progress > 0.25) {
            line(frame, new Point(x+w, y), new Point(x+w-cl, y),   cVerde, th, LINE_8, 0);
            line(frame, new Point(x+w, y), new Point(x+w,   y+cl), cVerde, th, LINE_8, 0);
        }
        if (progress > 0.5) {
            line(frame, new Point(x,   y+h), new Point(x+cl,   y+h), cVerde, th, LINE_8, 0);
            line(frame, new Point(x,   y+h), new Point(x,   y+h-cl), cVerde, th, LINE_8, 0);
        }
        if (progress > 0.75) {
            line(frame, new Point(x+w, y+h), new Point(x+w-cl, y+h), cVerde, th, LINE_8, 0);
            line(frame, new Point(x+w, y+h), new Point(x+w, y+h-cl), cVerde, th, LINE_8, 0);
        }

        if (progress > 0.35) {
            int foreY    = y + (int)(h * 0.12);
            int browY    = y + (int)(h * 0.30);
            int eyeY     = y + (int)(h * 0.38);
            int noseTopY = y + (int)(h * 0.46);
            int noseMidY = y + (int)(h * 0.55);
            int noseBotY = y + (int)(h * 0.62);
            int filtroY  = y + (int)(h * 0.67);
            int mouthY   = y + (int)(h * 0.73);
            int labioY   = y + (int)(h * 0.80);
            int chinY    = y + (int)(h * 0.92);
            int noseX    = x + (int)(w * 0.50);
            int lEye     = x + (int)(w * 0.33);
            int rEye     = x + (int)(w * 0.67);
            int lBrow    = x + (int)(w * 0.30);
            int rBrow    = x + (int)(w * 0.70);
            int lBrowIn  = x + (int)(w * 0.40);
            int rBrowIn  = x + (int)(w * 0.60);
            int lEyeL    = x + (int)(w * 0.23);
            int lEyeR    = x + (int)(w * 0.42);
            int rEyeL    = x + (int)(w * 0.58);
            int rEyeR    = x + (int)(w * 0.77);
            int noseL    = x + (int)(w * 0.43);
            int noseR    = x + (int)(w * 0.57);
            int nostrilL = x + (int)(w * 0.40);
            int nostrilR = x + (int)(w * 0.60);
            int mouthL   = x + (int)(w * 0.37);
            int mouthR   = x + (int)(w * 0.63);
            int lCheek   = x + (int)(w * 0.15);
            int rCheek   = x + (int)(w * 0.85);
            int lJaw     = x + (int)(w * 0.12);
            int rJaw     = x + (int)(w * 0.88);
            int lJawLow  = x + (int)(w * 0.22);
            int rJawLow  = x + (int)(w * 0.78);
            int lTemple  = x + (int)(w * 0.05);
            int rTemple  = x + (int)(w * 0.95);
            int templY   = y + (int)(h * 0.25);
            double da = Math.min(1.0, (progress - 0.35) / 0.4);
            int dr = Math.max(1, (int)(3 * da));

            if (progress > 0.55) {
                int lt = 1;
                line(frame, new Point(lTemple, templY),           new Point(lJaw, browY+20),          cMalla, lt, LINE_8, 0);
                line(frame, new Point(rTemple, templY),           new Point(rJaw, browY+20),          cMalla, lt, LINE_8, 0);
                line(frame, new Point(lJaw, browY+20),            new Point(lCheek, eyeY+30),         cMalla, lt, LINE_8, 0);
                line(frame, new Point(rJaw, browY+20),            new Point(rCheek, eyeY+30),         cMalla, lt, LINE_8, 0);
                line(frame, new Point(lCheek, eyeY+30),           new Point(lJawLow, noseBotY+10),    cMalla, lt, LINE_8, 0);
                line(frame, new Point(rCheek, eyeY+30),           new Point(rJawLow, noseBotY+10),    cMalla, lt, LINE_8, 0);
                line(frame, new Point(lJawLow, noseBotY+10),      new Point(x+(int)(w*0.32),chinY-10),cMalla, lt, LINE_8, 0);
                line(frame, new Point(rJawLow, noseBotY+10),      new Point(x+(int)(w*0.68),chinY-10),cMalla, lt, LINE_8, 0);
                line(frame, new Point(x+(int)(w*0.32), chinY-10), new Point(noseX, chinY),            cMalla, lt, LINE_8, 0);
                line(frame, new Point(x+(int)(w*0.68), chinY-10), new Point(noseX, chinY),            cMalla, lt, LINE_8, 0);
                line(frame, new Point(noseX, foreY),  new Point(noseX, chinY),    cMalla, lt, LINE_8, 0);
                line(frame, new Point(lBrow,  browY),  new Point(rBrow, browY),   cMalla, lt, LINE_8, 0);
                line(frame, new Point(lEyeL,  eyeY),   new Point(rEyeR, eyeY),   cMalla, lt, LINE_8, 0);
                line(frame, new Point(nostrilL,noseBotY),new Point(nostrilR,noseBotY),cMalla,lt,LINE_8,0);
                line(frame, new Point(mouthL, mouthY), new Point(mouthR, mouthY), cMalla, lt, LINE_8, 0);
                line(frame, new Point(lEyeL, eyeY), new Point(lEye, eyeY-8),  cMalla, lt, LINE_8, 0);
                line(frame, new Point(lEye, eyeY-8), new Point(lEyeR, eyeY),  cMalla, lt, LINE_8, 0);
                line(frame, new Point(lEyeR, eyeY), new Point(lEye, eyeY+6),  cMalla, lt, LINE_8, 0);
                line(frame, new Point(lEye, eyeY+6), new Point(lEyeL, eyeY),  cMalla, lt, LINE_8, 0);
                line(frame, new Point(rEyeL, eyeY), new Point(rEye, eyeY-8),  cMalla, lt, LINE_8, 0);
                line(frame, new Point(rEye, eyeY-8), new Point(rEyeR, eyeY),  cMalla, lt, LINE_8, 0);
                line(frame, new Point(rEyeR, eyeY), new Point(rEye, eyeY+6),  cMalla, lt, LINE_8, 0);
                line(frame, new Point(rEye, eyeY+6), new Point(rEyeL, eyeY),  cMalla, lt, LINE_8, 0);
                line(frame, new Point(lEyeL, browY+5), new Point(lBrow,   browY),   cMalla, lt, LINE_8, 0);
                line(frame, new Point(lBrow,  browY),   new Point(lBrowIn, browY+3), cMalla, lt, LINE_8, 0);
                line(frame, new Point(rEyeR, browY+5), new Point(rBrow,   browY),   cMalla, lt, LINE_8, 0);
                line(frame, new Point(rBrow,  browY),   new Point(rBrowIn, browY+3), cMalla, lt, LINE_8, 0);
                line(frame, new Point(noseX, noseTopY), new Point(noseX, noseBotY),      cMalla, lt, LINE_8, 0);
                line(frame, new Point(noseX, noseTopY), new Point(lEye,  eyeY+5),        cMalla, lt, LINE_8, 0);
                line(frame, new Point(noseX, noseTopY), new Point(rEye,  eyeY+5),        cMalla, lt, LINE_8, 0);
                line(frame, new Point(noseX, noseMidY), new Point(noseL, noseBotY),      cMalla, lt, LINE_8, 0);
                line(frame, new Point(noseX, noseMidY), new Point(noseR, noseBotY),      cMalla, lt, LINE_8, 0);
                line(frame, new Point(noseL, noseBotY), new Point(nostrilL, noseBotY+5), cMalla, lt, LINE_8, 0);
                line(frame, new Point(noseR, noseBotY), new Point(nostrilR, noseBotY+5), cMalla, lt, LINE_8, 0);
                line(frame, new Point(nostrilL,noseBotY+5),new Point(nostrilR,noseBotY+5),cMalla,lt,LINE_8,0);
                line(frame, new Point(mouthL, mouthY), new Point(noseX, mouthY-5), cMalla, lt, LINE_8, 0);
                line(frame, new Point(mouthR, mouthY), new Point(noseX, mouthY-5), cMalla, lt, LINE_8, 0);
                line(frame, new Point(mouthL, mouthY), new Point(noseX, labioY),   cMalla, lt, LINE_8, 0);
                line(frame, new Point(mouthR, mouthY), new Point(noseX, labioY),   cMalla, lt, LINE_8, 0);
                line(frame, new Point(noseX, mouthY-5),new Point(noseX, filtroY),  cMalla, lt, LINE_8, 0);
                line(frame, new Point(lTemple,templY),  new Point(noseX, foreY),   cMalla, lt, LINE_8, 0);
                line(frame, new Point(rTemple,templY),  new Point(noseX, foreY),   cMalla, lt, LINE_8, 0);
                line(frame, new Point(lBrow,  browY),   new Point(noseX, foreY),   cMalla, lt, LINE_8, 0);
                line(frame, new Point(rBrow,  browY),   new Point(noseX, foreY),   cMalla, lt, LINE_8, 0);
                line(frame, new Point(lCheek,eyeY+30),new Point(mouthL,   mouthY),    cMalla,lt,LINE_8,0);
                line(frame, new Point(rCheek,eyeY+30),new Point(mouthR,   mouthY),    cMalla,lt,LINE_8,0);
                line(frame, new Point(lCheek,eyeY+30),new Point(nostrilL, noseBotY+5),cMalla,lt,LINE_8,0);
                line(frame, new Point(rCheek,eyeY+30),new Point(nostrilR, noseBotY+5),cMalla,lt,LINE_8,0);
            }

            circle(frame, new Point(noseX, foreY),    dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(lTemple,templY),  dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(rTemple,templY),  dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(lBrow,  browY),   dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(lBrowIn,browY+3), dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(rBrow,  browY),   dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(rBrowIn,browY+3), dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(lEye,   eyeY),    dr+2, cCyan,   FILLED, LINE_8, 0);
            circle(frame, new Point(rEye,   eyeY),    dr+2, cCyan,   FILLED, LINE_8, 0);
            circle(frame, new Point(lEyeL,  eyeY),    dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(lEyeR,  eyeY),    dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(lEye,   eyeY-8),  dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(lEye,   eyeY+6),  dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(rEyeL,  eyeY),    dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(rEyeR,  eyeY),    dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(rEye,   eyeY-8),  dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(rEye,   eyeY+6),  dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(noseX,  noseTopY),dr+1, cCyan,   FILLED, LINE_8, 0);
            circle(frame, new Point(noseX,  noseMidY),dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(noseL,  noseBotY),dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(noseR,  noseBotY),dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(nostrilL,noseBotY+5),dr, cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(nostrilR,noseBotY+5),dr, cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(mouthL, mouthY),  dr+1, cCyan,   FILLED, LINE_8, 0);
            circle(frame, new Point(mouthR, mouthY),  dr+1, cCyan,   FILLED, LINE_8, 0);
            circle(frame, new Point(noseX,  mouthY-5),dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(noseX,  labioY),  dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(noseX,  filtroY), dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(lCheek, eyeY+30), dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(rCheek, eyeY+30), dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(lJaw,   browY+20),dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(rJaw,   browY+20),dr,   cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(lJawLow,noseBotY+10),dr, cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(rJawLow,noseBotY+10),dr, cBlanco, FILLED, LINE_8, 0);
            circle(frame, new Point(x+(int)(w*0.32),chinY-10),dr,cBlanco,FILLED,LINE_8,0);
            circle(frame, new Point(x+(int)(w*0.68),chinY-10),dr,cBlanco,FILLED,LINE_8,0);
            circle(frame, new Point(noseX,  chinY),   dr+1, cCyan,   FILLED, LINE_8, 0);
        }

        if (progress > 0.05 && progress < 0.92) {
            int scanY = y + (int)(h * ((progress * 2.0) % 1.0));
            if (scanY >= y && scanY < y+h) {
                line(frame, new Point(x, scanY),   new Point(x+w, scanY),   cVerde,  2, LINE_8, 0);
                line(frame, new Point(x, scanY-2), new Point(x+w, scanY-2), cOscuro, 1, LINE_8, 0);
            }
        }

        String txt  = progress >= 1.0 ? "FACE DETECTED" : "SCANNING...";
        Scalar cTxt = progress >= 1.0 ? cVerde : cCyan;
        int    txtW = progress >= 1.0 ? 185 : 145;
        rectangle(frame, new Point(x, y-38), new Point(x+txtW, y-8),
                new Scalar(0,0,0,0), FILLED, LINE_8, 0);
        putText(frame, txt, new Point(x+4, y-12),
                FONT_HERSHEY_DUPLEX, 0.6, cTxt, 2, LINE_8, false);
    }

    public boolean captureSinglePhoto(int idAlumno, String nombreArchivo) {
        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) { System.err.println(" No se pudo abrir camara"); return false; }
        camera.set(3, 640); camera.set(4, 480);
        Mat frame = new Mat();
        RectVector faces = new RectVector();
        boolean captured = false;
        int attempts = 0;
        while (!captured && attempts < 30) {
            if (!camera.read(frame) || frame.empty()) { attempts++; continue; }
            Mat gray = new Mat();
            cvtColor(frame, gray, COLOR_BGR2GRAY);
            faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0,
                    new Size(100,100), new Size());
            if (faces.size() > 0) {
                Mat face = new Mat(gray, faces.get(0));
                String fn = TRAINING_DATA_PATH + nombreArchivo + ".jpg";
                imwrite(fn, face);
                System.out.println("Foto guardada: " + fn);
                captured = true;
            }
            attempts++;
            try { Thread.sleep(50); } catch (InterruptedException e) { e.printStackTrace(); }
        }
        camera.release();
        return captured;
    }

    public RostroExistenteResult verificarRostroDuplicado(int idAlumnoActual,
                                                          String dniActual) {
        if (!modeloCargado) {
            cargarModelo();
            if (!modeloCargado) return new RostroExistenteResult(false,-1,0.0,null);
        }
        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) return new RostroExistenteResult(false,-1,0.0,null);
        camera.set(3, 640); camera.set(4, 480);
        Mat frame = new Mat();
        RectVector faces = new RectVector();
        int intentos = 0;
        while (intentos < 20) {
            if (!camera.read(frame) || frame.empty()) { intentos++; continue; }
            Mat gray = new Mat();
            cvtColor(frame, gray, COLOR_BGR2GRAY);
            faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0,
                    new Size(100,100), new Size());
            if (faces.size() > 0) {
                Mat resized = new Mat();
                resize(new Mat(gray, faces.get(0)), resized, new Size(200,200));
                int[] label = new int[1]; double[] conf = new double[1];
                try {
                    faceRecognizer.predict(resized, label, conf);
                    if (conf[0] < 70 && label[0] != idAlumnoActual) {
                        camera.release();
                        return new RostroExistenteResult(true, label[0], conf[0], dniActual);
                    }
                } catch (Exception e) { /* continuar */ }
            }
            intentos++;
            try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
        }
        camera.release();
        return new RostroExistenteResult(false,-1,0.0,null);
    }

    public int buscarRostroSimilar(List<models.Alumno> todosAlumnos, int idAlumnoActual) {
        if (!modeloCargado) { cargarModelo(); if (!modeloCargado) return -1; }
        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) return -1;
        camera.set(3, 640); camera.set(4, 480);
        Mat frame = new Mat(); RectVector faces = new RectVector();
        int resultado = -1, intentos = 0;
        while (intentos < 25 && resultado == -1) {
            if (camera.read(frame) && !frame.empty()) {
                Mat gray = new Mat();
                cvtColor(frame, gray, COLOR_BGR2GRAY);
                faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0,
                        new Size(100,100), new Size());
                if (faces.size() > 0) {
                    Mat resized = new Mat();
                    resize(new Mat(gray, faces.get(0)), resized, new Size(200,200));
                    int[] label = new int[1]; double[] conf = new double[1];
                    try {
                        faceRecognizer.predict(resized, label, conf);
                        if (conf[0] < 70 && label[0] != idAlumnoActual)
                            resultado = label[0];
                    } catch (Exception e) { /* ignorar */ }
                }
            }
            intentos++;
            try { Thread.sleep(80); } catch (Exception e) {}
        }
        camera.release();
        return resultado;
    }

    public void trainModel() {
        File dir = new File(TRAINING_DATA_PATH);
        File[] imgs = dir.listFiles((d, n) ->
                (n.toLowerCase().endsWith(".jpg") || n.toLowerCase().endsWith(".png"))
                        && !n.equals("trainedModel.yml"));
        if (imgs == null || imgs.length == 0) {
            System.out.println("Sin imágenes"); return;
        }
        System.out.println(" Entrenando con " + imgs.length + " imágenes...");
        MatVector images = new MatVector(imgs.length);
        Mat labels = new Mat(imgs.length, 1, CV_32SC1);
        IntBuffer lb = labels.createBuffer();
        int idx = 0;
        for (File f : imgs) {
            String[] parts = f.getName().split("_");
            if (parts.length >= 3) {
                try {
                    int label = Integer.parseInt(
                            parts[2].replace(".jpg","").replace(".png",""));
                    Mat img = imread(f.getAbsolutePath(), IMREAD_GRAYSCALE);
                    if (!img.empty()) {
                        Mat r = new Mat();
                        resize(img, r, new Size(200,200));
                        images.put(idx, r);
                        lb.put(idx, label);
                        idx++;
                    }
                } catch (NumberFormatException e) { /* ignorar */ }
            }
        }
        if (idx > 0) {
            try {
                faceRecognizer.train(images, labels);
                faceRecognizer.save(TRAINING_DATA_PATH + "trainedModel.yml");
                modeloCargado = true;
                System.out.println(" Entrenado con " + idx + " imágenes");
            } catch (Exception e) { System.err.println(" Error: " + e.getMessage()); }
        }
    }

    public int recognizeFace(Mat frame) {
        if (!modeloCargado) { cargarModelo(); if (!modeloCargado) return -1; }
        Mat gray = new Mat();
        cvtColor(frame, gray, COLOR_BGR2GRAY);
        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0,
                new Size(80, 80), new Size());

        if (faces.size() > 0) {
            Mat resized = new Mat();
            resize(new Mat(gray, faces.get(0)), resized, new Size(200, 200));
            int[]    label = new int[1];
            double[] conf  = new double[1];
            try {
                faceRecognizer.predict(resized, label, conf);
                System.out.println("🔍 ID=" + label[0]
                        + "  Conf=" + String.format("%.1f", conf[0]));

                if (conf[0] < UMBRAL_RECONOCIMIENTO) {
                    return label[0];   //  Reconocido
                } else {
                    return -2;
                }
            } catch (Exception e) {
                System.err.println("❌ " + e.getMessage());
            }
        }
        return -1; // Sin rostro
    }

    public static class RostroExistenteResult {
        public final boolean existe;
        public final int     idAlumnoExistente;
        public final double  confianza;
        public final String  dniNuevo;
        public RostroExistenteResult(boolean existe, int id, double conf, String dni) {
            this.existe = existe; this.idAlumnoExistente = id;
            this.confianza = conf; this.dniNuevo = dni;
        }
    }
}