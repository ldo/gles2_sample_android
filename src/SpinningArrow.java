package nz.gen.geek_central.gles2_sample;
/*
    Sample animation--graphical display of a spinning arrow.

    Copyright 2012, 2013 by Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy of
    the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations under
    the License.
*/

import nz.gen.geek_central.GraphicsUseful.PaintBuilder;
import nz.gen.geek_central.GLUseful.Vec3f;
import nz.gen.geek_central.GLUseful.Mat4f;
import nz.gen.geek_central.GLUseful.Rotation;
import nz.gen.geek_central.GLUseful.GLUseful;
import nz.gen.geek_central.GLUseful.GeomBuilder;
import nz.gen.geek_central.GLUseful.Lathe;
import nz.gen.geek_central.GLUseful.GLView;
import static nz.gen.geek_central.GLUseful.GLUseful.gl;

public class SpinningArrow extends SampleAnimationCommon
  {
  /* parameters for arrow: */
    private static final float BodyThickness = 0.15f;
    private static final float HeadThickness = 0.3f;
    private static final float HeadLengthOuter = 0.7f;
    private static final float HeadLengthInner = 0.4f;
    private static final float BaseBevel = 0.2f * BodyThickness;
    private static final int NrSectors = 12;

    private final GLUseful.Color ArrowColor;

    private final GeomBuilder.Obj ArrowShape;
    private GLView Background;
    private Mat4f BGProjection;

    public static class ShadedSpinningArrow extends SpinningArrow
      {

        public ShadedSpinningArrow
          (
            android.content.Context ctx
          )
          {
            super(ctx, true);
          } /*ShadedSpinningArrow*/

      } /*ShadedSpinningArrow*/;

    public static class WireframeSpinningArrow extends SpinningArrow
      {

        public WireframeSpinningArrow
          (
            android.content.Context ctx
          )
          {
            super(ctx, false);
          } /*WireframeSpinningArrow*/

      } /*WireframeSpinningArrow*/;

    private SpinningArrow
      (
        android.content.Context ctx,
        boolean Shaded
      )
      /* note no GL calls are made in constructor */
      {
        super(ctx);
        final float OuterTiltCos =
            HeadThickness / (float)Math.hypot(HeadThickness, HeadLengthOuter);
        final float OuterTiltSin =
            HeadLengthOuter / (float)Math.hypot(HeadThickness, HeadLengthOuter);
        final float InnerTiltCos =
            HeadThickness / (float)Math.hypot(HeadThickness, HeadLengthInner);
        final float InnerTiltSin =
            HeadLengthInner / (float)Math.hypot(HeadThickness, HeadLengthInner);
        final Vec3f[] Points =
            new Vec3f[]
              {
                new Vec3f(0.0f, 1.0f, 0.0f),
                new Vec3f(HeadThickness, 1.0f - HeadLengthOuter, 0.0f),
                new Vec3f(BodyThickness, 1.0f - HeadLengthInner, 0.0f),
                new Vec3f(BodyThickness, BaseBevel - 1.0f, 0.0f),
                new Vec3f(BodyThickness - BaseBevel, -0.98f, 0.0f),
                  /* y-coord of -1.0 seems to produce gaps in rendering when base
                    is face-on to viewer */
                new Vec3f(0.0f, -1.0f, 0.0f),
              };
        final Vec3f[] Normals =
            new Vec3f[]
              {
                new Vec3f(OuterTiltSin, OuterTiltCos, 0.0f), /* tip */
                new Vec3f(InnerTiltSin, - InnerTiltCos, 0.0f), /* head */
                new Vec3f(1.0f, 0.0f, 0.0f), /* body */
                new Vec3f
                  (
                    (float)Math.sqrt(0.5f),
                    -(float)Math.sqrt(0.5f),
                    0.0f
                  ), /* bevel */
                new Vec3f(0.0f, -1.0f, 0.0f), /* base */
              };
        ArrowShape = Lathe.Make
              (
                /*Shaded =*/ Shaded,
                /*Points =*/
                    new Lathe.VertexFunc()
                      {
                        public Vec3f Get
                          (
                            int PointIndex
                          )
                          {
                            return
                                Points[PointIndex];
                          } /*Get*/
                      } /*VertexFunc*/,
                /*NrPoints = */ Points.length,
                /*Normal =*/
                    new Lathe.VectorFunc()
                      {
                        public Vec3f Get
                          (
                            int PointIndex,
                            int SectorIndex, /* 0 .. NrSectors - 1 */
                            boolean Upper
                              /* indicates which of two calls for each point (except for
                                start and end points, which only get one call each) to allow
                                for discontiguous shading */
                          )
                          {
                            final float FaceAngle =
                                (float)(2.0 * Math.PI * SectorIndex / NrSectors);
                            final Vec3f OrigNormal =
                                Normals[PointIndex - (Upper ? 0 : 1)];
                            return
                                new Vec3f
                                  (
                                    OrigNormal.x * (float)Math.cos(FaceAngle),
                                    OrigNormal.y,
                                    OrigNormal.x * (float)Math.sin(FaceAngle)
                                  );
                          } /*Get*/
                      } /*VectorFunc*/,
                /*TexCoord = */ null,
                /*VertexColor =*/ null,
                /*NrSectors =*/ NrSectors,
                /*Uniforms =*/
                    new GLUseful.ShaderVarDef[]
                        {
                            new GLUseful.ShaderVarDef("light_direction", GLUseful.ShaderVarTypes.VEC3),
                            new GLUseful.ShaderVarDef("light_brightness", GLUseful.ShaderVarTypes.FLOAT),
                            new GLUseful.ShaderVarDef("light_contrast", GLUseful.ShaderVarTypes.FLOAT),
                            new GLUseful.ShaderVarDef("vertex_color", GLUseful.ShaderVarTypes.COLOR3),
                        },
                /*VertexColorCalc =*/
                    (Shaded ?
                        "    float attenuate = 1.2 - 0.4 * gl_Position.z;\n" +
                        "    frag_color = vec4\n" +
                        "      (\n" +
                        "            vertex_color\n" +
                        "        *\n" +
                        "            attenuate\n" +
                        "        *\n" +
                        "            (\n" +
                        "                light_brightness\n" +
                        "            -\n" +
                        "                light_contrast\n" +
                        "            +\n" +
                        "                    light_contrast\n" +
                        "                *\n" +
                        "                    dot\n" +
                        "                      (\n" +
                        "                        normalize(model_view * vec4(vertex_normal, 1.0)).xyz,\n" +
                        "                        normalize(light_direction)\n" +
                        "                      )\n" +
                        "            ),\n" +
                        "        1.0\n" +
                        "      );\n" +
                      /* simpleminded non-specular lighting */
                        "    back_color = vec4(vec3(0.5, 0.5, 0.5) * attenuate, 1.0);\n"
                    :
                        "    float attenuate = 1.2 - 0.3 * gl_Position.z;\n" +
                        "    vec3 vertex_color = vec3(0.6, 0.6, 0.36);\n" +
                        "    frag_color = vec4(vertex_color * attenuate, 1.0);\n"
                    ),
                /*BindNow =*/ false
              );
        ArrowColor = new GLUseful.Color(ctx.getResources().getColor(R.color.arrow));
      } /*SpinningArrow*/

    @Override
    public void Bind()
      {
        ArrowShape.Bind();
        if (Background != null)
          {
            Background.Bind();
          } /*if*/
      } /*Bind*/

    @Override
    public void Unbind
      (
        boolean Release
          /* true iff GL context still valid, so explicitly free up allocated resources.
            false means GL context has gone (or is going), so simply forget allocated
            GL resources without making any GL calls. */
      )
      /* frees up GL resources associated with this object. */
      {
        ArrowShape.Unbind(Release);
        if (Background != null)
          {
            Background.Unbind(Release);
          } /*if*/
      } /*Unbind*/

    @Override
    public void Setup
      (
        int ViewWidth,
        int ViewHeight
      )
      {
        super.Setup(ViewWidth, ViewHeight);
        if (Background != null)
          {
            Background.Unbind(true);
          } /*if*/
        Background = new GLView
          (
            /*BitsWidth =*/ ViewSize,
            /*BitsHeight =*/ ViewSize,
            /*BindNow =*/ true
          );
          {
            final float ViewRadius = ViewSize / 2.0f;
            final android.graphics.Canvas g = Background.Draw;
            g.save();
            g.translate(ViewRadius, ViewRadius);
            g.drawColor(NullColor, android.graphics.PorterDuff.Mode.SRC);
              /* initialize all pixels to fully transparent */
            g.drawArc
              (
                /*oval =*/ new android.graphics.RectF(-ViewRadius, -ViewRadius, ViewRadius, ViewRadius),
                /*startAngle =*/ 0.0f,
                /*sweepAngle =*/ 360.0f,
                /*useCenter =*/ false,
                /*paint =*/ new PaintBuilder(true)
                    .setStyle(android.graphics.Paint.Style.FILL)
                    .setColor(ctx.getResources().getColor(R.color.background))
                    .get()
              );
              {
                final String TheText = "Background Text";
                final android.graphics.Paint TextPaint = new PaintBuilder(true)
                    .setTextSize(ctx.getResources().getDimension(R.dimen.background_text_size))
                    .setTextAlign(android.graphics.Paint.Align.CENTER)
                    .setColor(ctx.getResources().getColor(R.color.background_text))
                    .get();
                final android.graphics.Rect TextBounds = new android.graphics.Rect();
                TextPaint.getTextBounds(TheText, 0, TheText.length(), TextBounds);
                final float YOffset = - (TextBounds.bottom + TextBounds.top) / 2.0f;
                  /* for vertical centring */
                g.drawText(TheText, - ViewRadius / 2.0f, - ViewRadius / 2.0f + YOffset, TextPaint);
                g.drawText(TheText, ViewRadius / 2.0f, ViewRadius / 2.0f + YOffset, TextPaint);
              }
            g.restore();
            Background.DrawChanged();
          }
        BGProjection = Mat4f.scaling
          (
            /*sx =*/ ViewWidth > ViewHeight ? ViewHeight * 1.0f / ViewWidth : 1.0f,
            /*sy =*/ ViewHeight > ViewWidth ? ViewWidth * 1.0f / ViewHeight : 1.0f,
            /*sz =*/ 1.0f
          );
      } /*Setup*/

    protected void OnDraw
      (
        double AtTime
      )
      /* draws the arrow in its orientation according to the specified time. Setup
        must already have been called on current GL context. */
      {
        if (Background != null)
          {
            Background.Draw
              (
                /*Projection =*/ BGProjection,
                /*Left =*/ -1.0f,
                /*Bottom =*/ -1.0f,
                /*Right =*/ 1.0f,
                /*Top =*/ 1.0f,
                /*Depth =*/ 0.99f /*disappears on some devices at 1.0f*/
              );
          } /*if*/
        ArrowShape.Draw
          (
            /*ProjectionMatrix =*/ ProjectionMatrix,
            /*ModelViewMatrix =*/
                new Rotation
                  (
                    /*angle =*/ (float)(AtTime * Math.PI),
                    /*degrees =*/ false,
                    /*axis =*/
                        new Rotation((float)(AtTime * Math.PI / 10.0), false, 0, 0, 1)
                            .GetMatrix()
                            .xform(new Vec3f(1, 0, 0))
                   ).GetMatrix()
                .mul(
                    Mat4f.scaling(2.0f, 2.0f, 2.0f)
                ),
            /*Uniforms =*/
                new GLUseful.ShaderVarVal[]
                    {
                        new GLUseful.ShaderVarVal("light_direction", new float[]{-0.7f, 0.7f, 0.0f}),
                        new GLUseful.ShaderVarVal("light_brightness", 1.0f),
                        new GLUseful.ShaderVarVal("light_contrast", 0.5f),
                        new GLUseful.ShaderVarVal("vertex_color", ArrowColor),
                    }
          );
      } /*OnDraw*/

  } /*SpinningArrow*/;
