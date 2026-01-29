package proyecto3;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.universe.SimpleUniverse;
import javax.media.j3d.*;
import javax.swing.*;
import java.awt.*;
import com.sun.j3d.utils.image.TextureLoader;
import javax.vecmath.Vector3f;
import javax.vecmath.Color3f;
import com.sun.j3d.utils.geometry.Sphere;
import javax.vecmath.Point3d; 
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.media.j3d.BoundingSphere;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

public class Proyecto3 extends JPanel { 
    private TransformGroup mouseGroup;
    private int[][] laberinto;
    
    private SegunderoPanel segunderoPanel;
    private boolean yaGane = false; 
    private Vector3f targetPosition = new Vector3f(-2.5f, 2.0f, -0.5f);
    
    public Proyecto3() {
        laberinto = new int[][]{
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0},
            {1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1},
            {1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1},
            {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1},
            {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1},
            {1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        };
        
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas3d = new Canvas3D(config);
        setLayout(new BorderLayout());
        add(canvas3d);

        SimpleUniverse universo = new SimpleUniverse(canvas3d);

        Transform3D vistaArriba = new Transform3D();
        vistaArriba.lookAt(
                new javax.vecmath.Point3d(0, 10, 0), 
                new javax.vecmath.Point3d(0, 0, 0), 
                new javax.vecmath.Vector3d(0, 0, -1) 
        );
        vistaArriba.invert(); 
        universo.getViewingPlatform().getViewPlatformTransform().setTransform(vistaArriba);

        BranchGroup escena = crearGrafoEscena();
        escena.compile();

        universo.addBranchGraph(escena);
        
        canvas3d.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                moverEsfera(e);
            }
        });
        canvas3d.setFocusable(true);
    }

    public BranchGroup crearGrafoEscena() {
        BranchGroup objetoRaiz = new BranchGroup();
        
        String rutaImagenFondo = "src/proyecto3/img/fondo.jpg";
        TextureLoader cargadorFondo = new TextureLoader(rutaImagenFondo, this);
        ImageComponent2D imagenFondo = cargadorFondo.getImage();

        if (imagenFondo != null) {
            Background fondo = new Background();
            fondo.setImage(imagenFondo);
            fondo.setApplicationBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0));
            fondo.setImageScaleMode(Background.SCALE_FIT_MAX);
            objetoRaiz.addChild(fondo);
        } else {
            System.out.println("No se pudo cargar la imagen de fondo.");
        }

        String rutaTexturaCubo = "src/proyecto3/img/pared.jpg";

        float tamCubo = 0.55f; 
        float desplazamientoX = 0.4f;
        float desplazamientoZ = -0.3f; 

        for (int fila = 0; fila < laberinto.length; fila++) {
            for (int col = 0; col < laberinto[fila].length; col++) {
                    if (laberinto[fila][col] == 1) {
                        float x = col * tamCubo - (laberinto[0].length / 2.0f * tamCubo) + desplazamientoX;
                        float z = fila * tamCubo - (laberinto.length / 2.0f * tamCubo) + desplazamientoZ;
                        objetoRaiz.addChild(crearCuboConTextura(x, 0.0f, z, rutaTexturaCubo));
                    }
                }
        }
        
        mouseGroup = new TransformGroup();
        mouseGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        mouseGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objetoRaiz.addChild(mouseGroup);

        Transform3D transformEsfera = new Transform3D();
        transformEsfera.setTranslation(new Vector3f(-2.5f, 2.0f, -0.5f));

        mouseGroup.setTransform(transformEsfera);

        TransformGroup sphereGroup = new TransformGroup();
        sphereGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        Sphere esfera = new Sphere(0.2f, Sphere.GENERATE_TEXTURE_COORDS, 50, app());
        sphereGroup.addChild(esfera);

        RotacionEsfera rotacion = new RotacionEsfera(sphereGroup);
        sphereGroup.addChild(rotacion);

        mouseGroup.addChild(sphereGroup);

        MouseRotate mr = new MouseRotate();
        mr.setTransformGroup(mouseGroup);
        mr.setSchedulingBounds(new BoundingSphere(new Point3d(), 1000f));
        objetoRaiz.addChild(mr);
        
        objetoRaiz.addChild(crearLuz());
                
        return objetoRaiz;
    }
    
    public void setSegunderoPanel(SegunderoPanel segunderoPanel) {
        this.segunderoPanel = segunderoPanel;
    }
    
    private TransformGroup crearCuboConTextura(float x, float y, float z, String rutaTextura) {
        Appearance apariencia = crearApariencia(rutaTextura);

        float tamañoCubo = 0.27f; 
        Box cubo = new Box(tamañoCubo, tamañoCubo, tamañoCubo, Box.GENERATE_TEXTURE_COORDS, apariencia);

        Transform3D transform = new Transform3D();
        transform.setTranslation(new Vector3f(x, y, z));
        TransformGroup tg = new TransformGroup(transform);
        tg.addChild(cubo);

        return tg;
    }
    
    private Appearance crearApariencia(String rutaTextura) {
        TextureLoader cargadorTextura = new TextureLoader(rutaTextura, this);
        Texture textura = cargadorTextura.getTexture();

        Appearance apariencia = new Appearance();
        apariencia.setTexture(textura);

        TextureAttributes atributosTextura = new TextureAttributes();
        atributosTextura.setTextureMode(TextureAttributes.MODULATE); 
        apariencia.setTextureAttributes(atributosTextura);

        Material material = new Material();
        apariencia.setMaterial(material);

        return apariencia;
    }

    private Light crearLuz() {
        DirectionalLight luz = new DirectionalLight(
                new Color3f(2.0f, 2.0f, 2.0f),
                new Vector3f(-1.0f, -1.0f, -1.0f)   
        );
        luz.setInfluencingBounds(new BoundingSphere());
        return luz;
    }
    
    Appearance app() {
        Appearance apariencia = new Appearance();

        TexCoordGeneration texCoord = new TexCoordGeneration(
                TexCoordGeneration.OBJECT_LINEAR,
                TexCoordGeneration.TEXTURE_COORDINATE_2);
        apariencia.setTexCoordGeneration(texCoord);

        String ladrillo = "src/proyecto3/img/esfera2.jpg";
        TextureLoader loader = new TextureLoader(ladrillo, this);
        ImageComponent2D imagen = loader.getImage();

        Texture2D textura = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
                imagen.getWidth(), imagen.getHeight());
        textura.setImage(0, imagen);
        textura.setEnable(true);

        textura.setMagFilter(Texture.BASE_LEVEL_LINEAR);
        textura.setMinFilter(Texture.BASE_LEVEL_LINEAR);

        apariencia.setTexture(textura);
        apariencia.setTextureAttributes(new TextureAttributes());
        
        Material material = new Material(
            new Color3f(0.8f, 0.8f, 0.8f),
            new Color3f(0.1f, 0.1f, 0.1f),
            new Color3f(0.8f, 0.8f, 0.8f),
            new Color3f(1.0f, 1.0f, 1.0f),
            64.0f
        );
        apariencia.setMaterial(material);
        
        return apariencia;
    }
    
    private void moverEsfera(KeyEvent e) {
        if (mouseGroup == null) {
            System.out.println("mouseGroup es null");
            return;
        }

        Transform3D transformEsfera = new Transform3D();
        mouseGroup.getTransform(transformEsfera);

        Vector3f posicion = new Vector3f();
        transformEsfera.get(posicion);
        
        float desplazamiento = 0.2f;

        float nuevaPosX = posicion.x;
        float nuevaPosZ = posicion.z;

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            nuevaPosZ -= desplazamiento; //ARRIBA
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            nuevaPosZ += desplazamiento; //ABAJO
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            nuevaPosX -= desplazamiento; //IZQUIERDA
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            nuevaPosX += desplazamiento; //DERECHA
        }

        if (segunderoPanel != null) {
            segunderoPanel.iniciarContador();
        }

        
        if (esLaberintoValido(nuevaPosX, nuevaPosZ)) {
            posicion.x = nuevaPosX;
            posicion.z = nuevaPosZ;
        } else {
            System.out.println("Choque con la pared!");
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                posicion.z += 0.05f; // EFECTO REBOTE
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                posicion.z -= 0.05f; // EFECTO REBOTE
            } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                posicion.x += 0.05f; // EFECTO REBOTE
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                posicion.x -= 0.05f; // EFECTO REBOTE
            }
        }


        float posicionY = posicion.y;

        transformEsfera.setTranslation(posicion);
        mouseGroup.setTransform(transformEsfera);
        
        System.out.println("Posición actual de la esfera -> X: " + posicion.x + ", Z: " + posicion.z);
        
        if (isGanador(posicion.x)) {
            yaGane = true;
            ganado();
            segunderoPanel.detenerContador();
        }
    }
    
    private boolean isGanador(float posX) {
        float objetivo = 2.800001f;
        float epsilon = 0.05f;

        return Math.abs(posX - objetivo) < epsilon;
    }
    
    public void ganado() {        
        int respuesta = JOptionPane.showConfirmDialog(
            null,
            "¡Has Ganado! ¿Quieres reiniciar el juego?",
            "Juego terminado",
            JOptionPane.YES_NO_OPTION
        );

        if (respuesta == JOptionPane.YES_OPTION) {
            reiniciarJuego();
        } else {
            JOptionPane.showMessageDialog(this, "Gracias por jugar.");
            System.exit(0);
        }
    }
    public void establecerGanado() {
        yaGane = true;
    }

    public boolean verificarVictoria() {
        return yaGane;
    }

    public void perderJuego() {
        int respuesta = JOptionPane.showConfirmDialog(
            this,
            "¡Has perdido! ¿Quieres reiniciar el juego?",
            "Juego terminado",
            JOptionPane.YES_NO_OPTION
        );

        if (respuesta == JOptionPane.YES_OPTION) {
            reiniciarJuego();
        } else {
            JOptionPane.showMessageDialog(this, "Gracias por jugar.");
            System.exit(0);
        }
    }
    
    private void reiniciarJuego() {
        if (segunderoPanel != null) {
            segunderoPanel.resetearContador();
        }

        // Resetear la posición de la esfera
        Transform3D transformEsfera = new Transform3D();
        transformEsfera.setTranslation(new Vector3f(-2.5f, 2.0f, -0.5f)); // Posición inicial de la esfera
        mouseGroup.setTransform(transformEsfera);

        System.out.println("Juego reiniciado");
    }
    
    private boolean esLaberintoValido(float x, float z) {
        float tamCubo = 0.55f; 
        float desplazamientoX = 0.4f; 
        float desplazamientoZ = -0.3f; 

        int col = Math.round((x + (laberinto[0].length / 2.0f * tamCubo) - desplazamientoX) / tamCubo);
        int fila = Math.round((z + (laberinto.length / 2.0f * tamCubo) - desplazamientoZ) / tamCubo);

        System.out.println("Posición X: " + x + ", Z: " + z + " -> Fila: " + fila + ", Columna: " + col);

        if (fila >= 0 && fila < laberinto.length && col >= 0 && col < laberinto[0].length) {
            return laberinto[fila][col] == 0; 
        }
        return false;
    }
    
    private boolean estaEnLaberinto(float x, float z) {
        float tamCubo = 0.55f;
        float desplazamientoX = 0.4f;
        float desplazamientoZ = -0.3f;

        int col = Math.round((x + (laberinto[0].length / 2.0f * tamCubo) - desplazamientoX) / tamCubo);
        int fila = Math.round((z + (laberinto.length / 2.0f * tamCubo) - desplazamientoZ) / tamCubo);

        return fila >= 0 && fila < laberinto.length && col >= 0 && col < laberinto[0].length && laberinto[fila][col] == 0;
    }

    public static void main(String[] args) {
        System.setProperty("sun.awt.noerasebackground", "true");
        JFrame ventana = new JFrame("PROYECTO TERCER PARCIAL");
        ventana.setSize(850, 800);
        ventana.setLocationRelativeTo(null);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Proyecto3 panel3D = new Proyecto3();
        SegunderoPanel segunderoPanel = new SegunderoPanel(panel3D);
        panel3D.setSegunderoPanel(segunderoPanel); // Configura la referencia aquí

        JPanel contenedorPrincipal = new JPanel(new BorderLayout());
        contenedorPrincipal.add(segunderoPanel, BorderLayout.NORTH);
        contenedorPrincipal.add(panel3D, BorderLayout.CENTER);

        ventana.setContentPane(contenedorPrincipal);
        ventana.setVisible(true);
    }    
}

class SegunderoPanel extends JPanel {
    private JLabel tiempoLabel;
    private int segundos = 0;
    private Timer timer;
    private Proyecto3 controlador;

    public SegunderoPanel(Proyecto3 controlador) {
        this.controlador = controlador; // Referencia al panel principal

        tiempoLabel = new JLabel("00:00");
        tiempoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        setLayout(new BorderLayout());
        add(tiempoLabel, BorderLayout.EAST);

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                segundos++;
                actualizarSegundos();

                if (segundos >= 20) {
                    timer.stop();
                    controlador.perderJuego();
                }
                if (controlador.verificarVictoria()) { 
                    System.out.println("¡Ganaste!");
                    timer.stop();
                }
            }
        });
    }

    private void actualizarSegundos() {
        int minutos = segundos / 60;
        int segs = segundos % 60;
        tiempoLabel.setText(String.format("%02d:%02d", minutos, segs));
    }

    public void iniciarContador() {
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    public void detenerContador() {
        timer.stop();
    }
    
    public void resetearContador() {
        timer.stop();
        segundos = 0;
        actualizarSegundos();
    }
}
