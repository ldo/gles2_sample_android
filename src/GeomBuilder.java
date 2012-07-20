package nz.gen.geek_central.GLUseful;
/*
    Easy construction and application of buffers needed for OpenGL-ES drawing.
    This version is for OpenGL-ES 2.0 and allows customization of the vertex
    shader for control of material properties, lighting etc.

    Copyright 2011, 2012 by Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

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

import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.ByteOrder;
import nz.gen.geek_central.GLUseful.GLUseful;

public class GeomBuilder
  /*
    Helper class for easier construction of geometrical
    objects. Instantiate this and tell it whether each vertex will
    also have a normal vector, a texture-coordinate vector or a
    colour. Then call Add to add vertex definitions (using class Vec3f
    to define points, and GeomBuilder.Color to define colours), and
    use the returned vertex indices to construct faces with AddTri and
    AddQuad. Finally, call MakeObj to obtain a GeomBuilder.Obj that
    has a Draw method that will render the resulting geometry into a
    specified GL context.
  */
  {
    static final android.opengl.GLES20 gl = new android.opengl.GLES20(); /* for easier references */

    public static class Color
      /* RGB colours with transparency */
      {
        public final float r, g, b, a;

        public Color
          (
            float r,
            float g,
            float b,
            float a
          )
          {
            this.r = r;
            this.b = b;
            this.g = g;
            this.a = a;
          } /*Color*/

      } /*Color*/

    private final ArrayList<Vec3f> Points;
    private final ArrayList<Vec3f> PointNormals;
    private final ArrayList<Vec3f> PointTexCoords;
    private final ArrayList<Color> PointColors;
    private final ArrayList<Integer> Faces;
    private Vec3f BoundMin, BoundMax;

    public GeomBuilder
      (
        boolean GotNormals, /* vertices will have normals specified */
        boolean GotTexCoords, /* vertices will have texture coordinates specified */
        boolean GotColors /* vertices will have colours specified */
      )
      {
        Points = new ArrayList<Vec3f>();
        PointNormals = GotNormals ? new ArrayList<Vec3f>() : null;
        PointTexCoords = GotTexCoords ? new ArrayList<Vec3f>() : null;
        PointColors = GotColors ? new ArrayList<Color>() : null;
        Faces = new ArrayList<Integer>();
        BoundMin = null;
        BoundMax = null;
      } /*GeomBuilder*/

    public int Add
      (
        Vec3f Vertex,
      /* following args are either mandatory or must be null, depending
        on respective flags passed to constructor */
        Vec3f Normal,
        Vec3f TexCoord,
        Color VertexColor
      )
      /* adds a new vertex, and returns its index for use in constructing faces. */
      {
        if
          (
                (PointNormals == null) != (Normal == null)
            ||
                (PointColors == null) != (VertexColor == null)
            ||
                (PointTexCoords == null) != (TexCoord == null)
          )
          {
            throw new RuntimeException("missing or redundant args specified");
          } /*if*/
        final int Result = Points.size();
        Points.add(Vertex);
        if (PointNormals != null)
          {
            PointNormals.add(Normal);
          } /*if*/
        if (PointTexCoords != null)
          {
            PointTexCoords.add(TexCoord);
          } /*if*/
        if (PointColors != null)
          {
            PointColors.add(VertexColor);
          } /*if*/
        if (BoundMin != null)
          {
            BoundMin =
                new Vec3f
                  (
                    Math.min(BoundMin.x, Vertex.x),
                    Math.min(BoundMin.y, Vertex.y),
                    Math.min(BoundMin.z, Vertex.z)
                  );
          }
        else
          {
            BoundMin = Vertex;
          } /*if*/
        if (BoundMax != null)
          {
            BoundMax =
                new Vec3f
                  (
                    Math.max(BoundMax.x, Vertex.x),
                    Math.max(BoundMax.y, Vertex.y),
                    Math.max(BoundMax.z, Vertex.z)
                  );
          }
        else
          {
            BoundMax = Vertex;
          } /*if*/
        return
            Result;
      } /*Add*/

    public void AddTri
      (
        int V1,
        int V2,
        int V3
      )
      /* defines a triangular face. Args are indices as previously returned from calls to Add. */
      {
        Faces.add(V1);
        Faces.add(V2);
        Faces.add(V3);
      } /*AddTri*/

    public void AddQuad
      (
        int V1,
        int V2,
        int V3,
        int V4
      )
      /* Defines a quadrilateral face. Args are indices as previously returned from calls to Add. */
      {
        AddTri(V1, V2, V3);
        AddTri(V4, V1, V3);
      } /*AddQuad*/

    public void AddPoly
      (
        int[] V
      )
      /* Defines a polygonal face. Array elements are indices as previously
        returned from calls to Add. */
      {
        for (int i = 1; i < V.length - 1; ++i)
          {
            AddTri(V[0], V[i], V[i + 1]);
          } /*for*/
      } /*AddPoly*/

    public enum ShaderVarTypes
      {
        TYPE_FLOAT,
        TYPE_VEC3,
      } /*ShaderVarTypes*/;

    public static class ShaderVarDef
      /* definition of a user shader variable */
      {
        public final String Name;
        public final ShaderVarTypes Type;

        public ShaderVarDef
          (
            String Name,
            ShaderVarTypes Type
          )
          {
            this.Name = Name.intern();
            this.Type = Type;
          } /*ShaderVarDef*/

      } /*ShaderVarDef*/;

    public static class ShaderVarVal
      /* specification of the value for a user shader variable */
      {
        public final String Name;
        public final Object Value;
          /* Float for TYPE_FLOAT, array of 3 floats for TYPE_VEC3 */

        public ShaderVarVal
          (
            String Name,
            Object Value
          )
          {
            this.Name = Name.intern();
            this.Value = Value;
          } /*ShaderVarVal*/

      } /*ShaderVarVal*/;

    public static class Obj
      /* representation of complete object geometry. */
      {
        private final IntBuffer VertexBuffer;
        private final IntBuffer NormalBuffer;
        private final IntBuffer TexCoordBuffer;
        private final IntBuffer ColorBuffer;
        private final ShortBuffer IndexBuffer;
        private final int NrIndexes;
        private final GLUseful.Program Render;
        public final Vec3f BoundMin, BoundMax;

        private final int ModelViewTransformVar, ProjectionTransformVar;
        private final int VertexPositionVar, VertexNormalVar, VertexColorVar;

        private static class UniformInfo
          {
            public final ShaderVarTypes Type;
            public final int Loc;

            public UniformInfo
              (
                ShaderVarTypes Type,
                int Loc
              )
              {
                this.Type = Type;
                this.Loc = Loc;
              } /*UniformInfo*/

          } /*UniformInfo*/;

        private final java.util.HashMap<String, UniformInfo> UniformLocs;

        private Obj
          (
            IntBuffer VertexBuffer,
            IntBuffer NormalBuffer, /* optional */
            IntBuffer TexCoordBuffer, /* optional, NYI */
            IntBuffer ColorBuffer, /* optional */
            ShortBuffer IndexBuffer,
            int NrIndexes,
            ShaderVarDef[] Uniforms,
              /* optional additional uniform variable definitions for vertex shader */
            String VertexColorCalc,
              /* optional, compiled as part of vertex shader to implement lighting etc, must
                assign values to "front_color" and "back_color" variables */
            Vec3f BoundMin,
            Vec3f BoundMax
          )
          {
            this.VertexBuffer = VertexBuffer;
            this.NormalBuffer = NormalBuffer;
            this.TexCoordBuffer = TexCoordBuffer;
            this.ColorBuffer = ColorBuffer;
            this.IndexBuffer = IndexBuffer;
            this.NrIndexes = NrIndexes;
            this.BoundMin = BoundMin;
            this.BoundMax = BoundMax;
            final StringBuilder VS = new StringBuilder();
            VS.append("uniform mat4 model_view, projection;\n");
            VS.append("attribute vec3 vertex_position;\n");
            if (NormalBuffer != null)
              {
                VS.append("attribute vec3 vertex_normal;\n");
              } /*if*/
            if (TexCoordBuffer != null)
              {
                VS.append("attribute vec3 vertex_texcoord;\n");
              } /*if*/
            if (ColorBuffer != null)
              {
                VS.append("attribute vec3 vertex_color;\n");
              } /*if*/
            if (Uniforms != null)
              {
                for (ShaderVarDef VarDef : Uniforms)
                  {
                    VS.append("uniform ");
                    switch (VarDef.Type)
                      {
                    case TYPE_FLOAT:
                        VS.append("float");
                    break;
                    case TYPE_VEC3:
                        VS.append("vec3");
                    break;
                      } /*switch*/
                    VS.append(" ");
                    VS.append(VarDef.Name);
                    VS.append(";\n");
                  } /*for*/
              } /*if*/
            VS.append("varying vec4 front_color, back_color;\n");
            VS.append("\n");
            VS.append("void main()\n");
            VS.append("  {\n");
            VS.append("    gl_Position = projection * model_view * vec4(vertex_position, 1.0);\n");
            if (VertexColorCalc != null)
              {
                VS.append(VertexColorCalc);
              }
            else
              {
                if (ColorBuffer != null)
                  {
                    VS.append("    front_color = vertex_color;\n");
                  }
                else
                  {
                    VS.append("    front_color = vec4(0.5, 0.5, 0.5, 1.0);\n");
                  } /*if*/
                VS.append("    back_color = vec4(0.5, 0.5, 0.5, 1.0);\n");
              } /*if*/
            VS.append("  } /*main*/\n");
          /* use of vertex_texcoord NYI */
            Render = new GLUseful.Program
              (
              /* vertex shader: */
                VS.toString(),
              /* fragment shader: */
                    "precision mediump float;\n" +
                    "varying vec4 front_color, back_color;\n" +
                    "\n" +
                    "void main()\n" +
                    "  {\n" +
                    "    if (gl_FrontFacing)\n" +
                    "        gl_FragColor = front_color;\n" +
                    "    else\n" +
                    "        gl_FragColor = back_color;\n" +
                    "  } /*main*/\n"
              );
            ModelViewTransformVar = Render.GetUniform("model_view", true);
            ProjectionTransformVar = Render.GetUniform("projection", true);
            VertexPositionVar = Render.GetAttrib("vertex_position", true);
            VertexNormalVar = Render.GetAttrib("vertex_normal", false);
            VertexColorVar = Render.GetAttrib("vertex_color", false);
            if (Uniforms != null)
              {
                UniformLocs = new java.util.HashMap<String, UniformInfo>();
                for (ShaderVarDef VarDef : Uniforms)
                  {
                    UniformLocs.put
                      (
                        VarDef.Name,
                        new UniformInfo(VarDef.Type, Render.GetUniform(VarDef.Name, false))
                      );
                  } /*for*/
              }
            else
              {
                UniformLocs = null;
              } /*if*/
          } /*Obj*/

        public void Draw
          (
            Mat4f ProjectionMatrix,
            Mat4f ModelViewMatrix,
            ShaderVarVal[] Uniforms /* optional additional values for uniforms */
          )
          /* actually renders the geometry into the current GL context. */
          {
            Render.Use();
            gl.glUniformMatrix4fv(ProjectionTransformVar, 1, false, ProjectionMatrix.to_floats(true, 16), 0);
            gl.glUniformMatrix4fv(ModelViewTransformVar, 1, false, ModelViewMatrix.to_floats(true, 16), 0);
            if ((Uniforms != null) != (UniformLocs != null))
              {
                throw new RuntimeException("uniform defs/vals mismatch");
              } /*if*/
            gl.glEnableVertexAttribArray(VertexPositionVar);
            gl.glVertexAttribPointer(VertexPositionVar, 3, gl.GL_FIXED, true, 0, VertexBuffer);
            if (NormalBuffer != null)
              {
                gl.glEnableVertexAttribArray(VertexNormalVar);
                gl.glVertexAttribPointer(VertexNormalVar, 3, gl.GL_FIXED, true, 0, NormalBuffer);
              } /*if*/
            if (ColorBuffer != null)
              {
                gl.glEnableVertexAttribArray(VertexColorVar);
                gl.glVertexAttribPointer(VertexColorVar, 4, gl.GL_FIXED, true, 0, ColorBuffer);
              } /*if*/
            if (Uniforms != null)
              {
                for (ShaderVarVal VarRef : Uniforms)
                  {
                    final UniformInfo VarInfo = UniformLocs.get(VarRef.Name);
                    if (VarInfo == null)
                      {
                        throw new RuntimeException("no such uniform variable “" + VarRef.Name + "”");
                      } /*if*/
                    switch (VarInfo.Type)
                      {
                    case TYPE_FLOAT:
                        gl.glUniform1f(VarInfo.Loc, (Float)VarRef.Value);
                    break;
                    case TYPE_VEC3:
                          {
                            final float[] Value = (float[])VarRef.Value;
                            gl.glUniform3f(VarInfo.Loc, Value[0], Value[1], Value[2]);
                          }
                    break;
                      } /*switch*/
                  } /*for*/
              } /*if*/
            gl.glDrawElements(gl.GL_TRIANGLES, NrIndexes, gl.GL_UNSIGNED_SHORT, IndexBuffer);
            Render.Unuse();
          } /*Draw*/

        public void Release()
          {
            Render.Release();
          } /*Release*/

      } /*Obj*/;

    public Obj MakeObj
      (
        ShaderVarDef[] Uniforms,
          /* optional additional uniform variable definitions for vertex shader */
        String VertexColorCalc
          /* optional, compiled as part of vertex shader to implement lighting etc, must
            assign values to "front_color" and "back_color" variables */
      )
      /* constructs and returns the final geometry ready for rendering. */
      {
        if (Points.size() == 0)
          {
            throw new RuntimeException("GeomBuilder: empty object");
          } /*if*/
        final int Fixed1 = 0x10000;
        final int[] Vertices = new int[Points.size() * 3];
        final int[] Normals = PointNormals != null ? new int[Points.size() * 3] : null;
        final int[] TexCoords = PointTexCoords != null ? new int[Points.size() * 3] : null;
        final int[] Colors = PointColors != null ? new int[Points.size() * 4] : null;
        int jv = 0, jn = 0, jt = 0, jc = 0;
        for (int i = 0; i < Points.size(); ++i)
          {
            final Vec3f Point = Points.get(i);
            Vertices[jv++] = (int)(Point.x * Fixed1);
            Vertices[jv++] = (int)(Point.y * Fixed1);
            Vertices[jv++] = (int)(Point.z * Fixed1);
            if (PointNormals != null)
              {
                final Vec3f PointNormal = PointNormals.get(i);
                Normals[jn++] = (int)(PointNormal.x * Fixed1);
                Normals[jn++] = (int)(PointNormal.y * Fixed1);
                Normals[jn++] = (int)(PointNormal.z * Fixed1);
              } /*if*/
            if (PointTexCoords != null)
              {
                final Vec3f Coord = PointTexCoords.get(i);
                TexCoords[jt++] = (int)(Coord.x * Fixed1);
                TexCoords[jt++] = (int)(Coord.y * Fixed1);
                TexCoords[jt++] = (int)(Coord.z * Fixed1);
              } /*if*/
            if (PointColors != null)
              {
                final Color ThisColor = PointColors.get(i);
                Colors[jc++] = (int)(ThisColor.r * Fixed1);
                Colors[jc++] = (int)(ThisColor.g * Fixed1);
                Colors[jc++] = (int)(ThisColor.b * Fixed1);
                Colors[jc++] = (int)(ThisColor.a * Fixed1);
              } /*if*/
          } /*for*/
        final short[] Indices = new short[Faces.size()];
        final int NrIndexes = Indices.length;
        for (int i = 0; i < NrIndexes; ++i)
          {
            Indices[i] = (short)(int)Faces.get(i);
          } /*for*/
      /* Need to use allocateDirect to allocate buffers so garbage
        collector won't move them. Also make sure byte order is
        always native. But direct-allocation and order-setting methods
        are only available for ByteBuffer. Which is why buffers
        are allocated as ByteBuffers and then converted to more
        appropriate types. */
        final IntBuffer VertexBuffer;
        final IntBuffer NormalBuffer;
        final IntBuffer TexCoordBuffer;
        final IntBuffer ColorBuffer;
        final ShortBuffer IndexBuffer;
        VertexBuffer =
            ByteBuffer.allocateDirect(Vertices.length * 4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
            .put(Vertices);
        VertexBuffer.position(0);
        if (PointNormals != null)
          {
            NormalBuffer =
                ByteBuffer.allocateDirect(Normals.length * 4)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer()
                .put(Normals);
            NormalBuffer.position(0);
          }
        else
          {
            NormalBuffer = null;
          } /*if*/
        if (PointTexCoords != null)
          {
            TexCoordBuffer =
                ByteBuffer.allocateDirect(TexCoords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer()
                .put(TexCoords);
            TexCoordBuffer.position(0);
          }
        else
          {
            TexCoordBuffer = null;
          } /*if*/
        if (PointColors != null)
          {
            ColorBuffer =
                ByteBuffer.allocateDirect(Colors.length * 4)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer()
                .put(Colors);
            ColorBuffer.position(0);
          }
        else
          {
            ColorBuffer = null;
          } /*if*/
        IndexBuffer =
            ByteBuffer.allocateDirect(Indices.length * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(Indices);
        IndexBuffer.position(0);
        return
            new Obj
              (
                VertexBuffer,
                NormalBuffer,
                TexCoordBuffer,
                ColorBuffer,
                IndexBuffer,
                NrIndexes,
                Uniforms,
                VertexColorCalc,
                BoundMin,
                BoundMax
              );
      } /*MakeObj*/

  } /*GeomBuilder*/
