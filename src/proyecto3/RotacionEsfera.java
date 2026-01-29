/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto3;

import javax.vecmath.Vector3f;
import javax.media.j3d.*;
import java.util.Enumeration;

public class RotacionEsfera extends Behavior {

    private TransformGroup tg;
    private float angulo = 0.0f;
    private WakeupCondition wakeupCondition;

    public RotacionEsfera(TransformGroup tg) {
        this.tg = tg; 
        setSchedulingBounds(new BoundingSphere());
        wakeupCondition = new WakeupOnElapsedTime(50);
    }

    @Override
    public void initialize() {
        angulo = 0.0f;
        wakeupOn(wakeupCondition);
    }

    @Override
    public void processStimulus(Enumeration criteria) {
        angulo += Math.toRadians(1);
        Transform3D transform = new Transform3D();
        transform.rotY(angulo);
        tg.setTransform(transform);
        
        wakeupOn(wakeupCondition);
    }
}
