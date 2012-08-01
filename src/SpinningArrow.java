package nz.gen.geek_central.gles2_sample;
/*
    Graphical display of a spinning arrow.

    Copyright 2012 by Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

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

import nz.gen.geek_central.GLUseful.Vec3f;
import nz.gen.geek_central.GLUseful.Mat4f;
import nz.gen.geek_central.GLUseful.GLUseful;
import nz.gen.geek_central.GLUseful.GeomBuilder;
import nz.gen.geek_central.GLUseful.Lathe;
import static nz.gen.geek_central.GLUseful.GLUseful.gl;

public class SpinningArrow
  {
  /* parameters for arrow: */
    private static final float BodyThickness = 0.15f;
    private static final float HeadThickness = 0.3f;
    private static final float HeadLengthOuter = 0.7f;
    private static final float HeadLengthInner = 0.4f;
    private static final float BaseBevel = 0.2f * BodyThickness;
    private static final int NrSectors = 12;

    private final GLUseful.Color ArrowColor;

    private final boolean Shaded;
    private GeomBuilder.Obj ArrowShape;
    private boolean SetupDone;
    private double StartTime,  LastDraw;
    Mat4f ProjectionMatrix;

    private static GeomBuilder.Obj MakeArrow
      (
        boolean Shaded
      )
      {
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
                    android.util.FloatMath.sqrt(0.5f),
                    -android.util.FloatMath.sqrt(0.5f),
                    0.0f
                  ), /* bevel */
                new Vec3f(0.0f, -1.0f, 0.0f), /* base */
              };
        return
            Lathe.Make
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
                                    OrigNormal.x * android.util.FloatMath.cos(FaceAngle),
                                    OrigNormal.y,
                                    OrigNormal.x * android.util.FloatMath.sin(FaceAngle)
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
                    )
              );
      } /*MakeArrow*/

    public SpinningArrow
      (
        android.content.Context ctx,
        boolean Shaded
      )
      {
        this.Shaded = Shaded;
        ArrowColor = new GLUseful.Color(ctx.getResources().getColor(R.color.arrow));
        StartTime = System.currentTimeMillis() / 1000.0;
      /* creation of GL objects cannot be done here, must wait until
        I have a GL context */
        SetupDone = false;
      } /*SpinningArrow*/

    public void Setup
      (
        int ViewWidth,
        int ViewHeight
      )
      /* initial setup for drawing that doesn't need to be done for every frame. */
      {
        if (!SetupDone)
          {
            ArrowShape = MakeArrow(Shaded);
            SetupDone = true;
          } /*if*/
        ProjectionMatrix =
                Mat4f.frustum
                  (
                    /*L =*/ - (float)ViewWidth / ViewHeight,
                    /*R =*/ (float)ViewWidth / ViewHeight,
                    /*B =*/ -1.0f,
                    /*T =*/ 1.0f,
                    /*N =*/ 1.0f,
                    /*F =*/ 10.0f
                  )
            .mul(
                Mat4f.translation(new Vec3f(0, 0, -3.0f))
            );
      } /*Setup*/

    public void Draw
      (
        double AtTime
      )
      /* draws the arrow in its orientation according to the specified time. Setup
        must already have been called on current GL context. */
      {
        final float Azi = (float)(Math.PI * AtTime);
        final float Elev = (float)(Math.PI * Math.sin(0.25 * Math.PI * AtTime));
        final float Roll = (float)(Math.PI / 10.0 * Math.sin(0.25 * Math.PI * AtTime));
        ArrowShape.Draw
          (
            /*ProjectionMatrix =*/ ProjectionMatrix,
            /*ModelViewMatrix =*/
                    Mat4f.rotation(Mat4f.AXIS_Z, Azi)
                .mul(
                    Mat4f.rotation(Mat4f.AXIS_X, Elev)
                ).mul(
                    Mat4f.rotation(Mat4f.AXIS_Y, Roll)
                ).mul(
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
      } /*Draw*/

    public void Draw()
      /* draws the arrow in its current orientation. */
      {
        LastDraw = System.currentTimeMillis() / 1000.0 - StartTime;
        Draw(LastDraw);
      } /*Draw*/

    public void DrawAgain()
      /* redraws the arrow in the same orientation as last time. */
      {
        Draw(LastDraw);
      } /*DrawAgain*/

    public double GetDrawTime()
      {
        return
            LastDraw;
      } /*GetDrawTime*/

    public void SetDrawTime
      (
        double AtTime /* ignored if negative */
      )
      {
        if (AtTime >= 0.0)
          {
            StartTime = System.currentTimeMillis() / 1000.0 - AtTime;
            LastDraw = AtTime;
          } /*if*/
      } /*SetDrawTime*/

    public void Release()
      /* frees up GL resources associated with this object. */
      {
        if (ArrowShape != null)
          {
            ArrowShape.Release();
          } /*if*/
        ArrowShape = null;
      } /*Release*/

  } /*SpinningArrow*/
