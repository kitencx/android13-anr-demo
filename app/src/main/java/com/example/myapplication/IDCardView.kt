
package com.example.myapplication

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator


class IDCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleId: Int = 0
) : View(context, attrs, defStyleId) {


    private val matrix = Matrix()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val DURATION = 2000f
    private val shaderAnimator = ValueAnimator.ofFloat(0f, Math.PI.toFloat())
//    private val agslCode = """
//        uniform float iTime;
//        uniform float2 iResolution;
//        vec4 main(vec2 fragCoord) {
//          vec4 o = vec4(0);
//          vec2 p = vec2(0), c=p, u=fragCoord.xy*2.-iResolution.xy;
//          float a;
//          for (float i=0; i<100; i++) {
//            a = i/2e2-1.;
//            p = cos(i*2.4 + iTime +vec2(0,11))*sqrt(1.-a*a);
//            c = u/iResolution.y+vec2(p.x,a)/(p.y+2.);
//            o += (cos(i+vec4(0,2,4,0))+1.)/dot(c,c)*(1.-p.y)/3e4;
//          }
//          return o;
//        }
//    """
//    private val agslCode = """
//        uniform float iTime;
//        uniform float2 iResolution;
//        mat2 rotate2D(float r){
//            return mat2(cos(r), sin(r), -sin(r), cos(r));
//        }
//
//        mat3 rotate3D(float angle, vec3 axis){
//            vec3 a = normalize(axis);
//            float s = sin(angle);
//            float c = cos(angle);
//            float r = 1.0 - c;
//            return mat3(
//                a.x * a.x * r + c,
//                a.y * a.x * r + a.z * s,
//                a.z * a.x * r - a.y * s,
//                a.x * a.y * r - a.z * s,
//                a.y * a.y * r + c,
//                a.z * a.y * r + a.x * s,
//                a.x * a.z * r + a.y * s,
//                a.y * a.z * r - a.x * s,
//                a.z * a.z * r + c
//            );
//        }
//
//        half4 main(float2 FC) {
//          vec4 o = vec4(0);
//          vec2 r = iResolution.xy;
//          vec3 v = vec3(1,3,7), p = vec3(0);
//          float t=iTime, n=0, e=0, g=0, k=t*.2;
//          for (float i=0; i<100; ++i) {
//            p = vec3((FC.xy-r*.5)/r.y*g,g)*rotate3D(k,cos(k+v));
//            p.z += t;
//            p = asin(sin(p)) - 3.;
//            n = 0;
//            for (float j=0; j<9.; ++j) {
//              p.xz *= rotate2D(g/8.);
//              p = abs(p);
//              p = p.x<p.y ? n++, p.zxy : p.zyx;
//              p += p-v;
//            }
//            g += e = max(p.x,p.z) / 1e3 - .01;
//            o.rgb += .1/exp(cos(v*g*.1+n)+3.+1e4*e);
//          }
//          return o.xyz1;
//        }
//    """.trimIndent()
    private val agslCode = """
        // Star Nest by Pablo Roman Andrioli

        // This content is under the MIT License.
        uniform float iTime;
        uniform float2 iResolution;
        uniform float2 iMouse;
        const int iterations = 17;
        const float formuparam = 0.53;

        const int volsteps = 20;
        const float stepsize = 0.1;

        const float zoom  = 0.800;
        const float tile  = 0.850;
        const float speed =0.010 ;

        const float brightness =0.0015;
        const float darkmatter =0.300;
        const float distfading =0.730;
        const float saturation =0.850;


        half4 main( in vec2 fragCoord )
        {
        	//get coords and direction
        	vec2 uv=fragCoord.xy/iResolution.xy-.5;
        	uv.y*=iResolution.y/iResolution.x;
        	vec3 dir=vec3(uv*zoom,1.);
        	float time=iTime*speed+.25;

        	//mouse rotation
        	float a1=.5+iMouse.x/iResolution.x*2.;
        	float a2=.8+iMouse.y/iResolution.y*2.;
        	mat2 rot1=mat2(cos(a1),sin(a1),-sin(a1),cos(a1));
        	mat2 rot2=mat2(cos(a2),sin(a2),-sin(a2),cos(a2));
        	dir.xz*=rot1;
        	dir.xy*=rot2;
        	vec3 from=vec3(1.,.5,0.5);
        	from+=vec3(time*2.,time,-2.);
        	from.xz*=rot1;
        	from.xy*=rot2;
        	
        	//volumetric rendering
        	float s=0.1,fade=1.;
        	vec3 v=vec3(0.);
        	for (int r=0; r<volsteps; r++) {
        		vec3 p=from+s*dir*.5;
        		p = abs(vec3(tile)-mod(p,vec3(tile*2.))); // tiling fold
        		float pa,a=pa=0.;
        		for (int i=0; i<iterations; i++) { 
        			p=abs(p)/dot(p,p)-formuparam; // the magic formula
        			a+=abs(length(p)-pa); // absolute sum of average change
        			pa=length(p);
        		}
        		float dm=max(0.,darkmatter-a*a*.001); //dark matter
        		a*=a*a; // add contrast
        		if (r>6) fade*=1.-dm; // dark matter, don't render near
        		//v+=vec3(dm,dm*.5,0.);
        		v+=fade;
        		v+=vec3(s,s*s,s*s*s*s)*a*brightness*fade; // coloring based on distance
        		fade*=distfading; // distance fading
        		s+=stepsize;
        	}
        	v=mix(vec3(length(v)),v,saturation); //color adjust
        	return vec4(v*.01,1.);	
        	
        }
    """.trimIndent()
    private val runtimeShader = RuntimeShader(agslCode)

    init {
        shaderAnimator.duration = DURATION.toLong()
        shaderAnimator.repeatCount = ValueAnimator.INFINITE
        shaderAnimator.repeatMode = ValueAnimator.RESTART
        shaderAnimator.interpolator = LinearInterpolator()
        paint.color = Color.RED

        // 设置抛物线参数
//        runtimeShader.setFloatUniform("iDuration", DURATION) // a = 0.01
        shaderAnimator.addUpdateListener { animation ->
            runtimeShader.setFloatUniform("iTime", animation.animatedValue as Float )
            invalidate()
        }
        runtimeShader.setFloatUniform("iMouse", 1F, 1F)
        shaderAnimator.start()

        // 创建Paint对象并设置Shader
        paint.setShader(runtimeShader)
//        setRenderEffect(RenderEffect.createBlurEffect(10F, 10F, Shader.TileMode.CLAMP))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        runtimeShader.setFloatUniform("iResolution", w.toFloat(), h.toFloat()) // w, h
    }

    private val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20F, resources.displayMetrics)
        color = Color.BLACK
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPaint(paint)
//        canvas.drawText("测试内容", width / 2F, height / 2F, textPaint)
//        canvas.drawRect(0F, 0F, 512F, 512F, paint)
    }
}