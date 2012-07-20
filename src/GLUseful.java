package nz.gen.geek_central.GLUseful;
/*
    Useful OpenGL-ES-2.0-related definitions.

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

public class GLUseful
  {
    public static final android.opengl.GLES20 gl = new android.opengl.GLES20(); /* for easier references */

    static
      {
        System.loadLibrary("gl_useful");
      } /*static*/

/*
    Error checking
*/

    public static void ThrowError
      (
        int Err,
        String DoingWhat
      )
      {
        throw new RuntimeException
          (
            String.format
              (
                "OpenGL error %d %s",
                Err,
                DoingWhat
              )
          );
      } /*ThrowError*/

    public static void ThrowError
      (
        String DoingWhat
      )
      {
        ThrowError(gl.glGetError(), DoingWhat);
      } /*ThrowError*/

    public static void CheckError
      (
        String DoingWhat
      )
      {
        int Err = gl.glGetError();
        if (Err != 0)
          {
            ThrowError(Err, DoingWhat);
          } /*if*/
      } /*CheckError*/

    public static native String GetShaderInfoLog
      (
        int ShaderID
      );
      /* needed because android.opengl.GLES20.glGetShaderInfoLog doesn't return anything,
        at least on Android 2.2. */

/*
    Shader programs
*/

    public static class Shader
      {
        public final int id;

        public Shader
          (
            int Type,
            String Source
          )
          {
            id = gl.glCreateShader(Type);
            if (id == 0)
              {
                ThrowError("creating shader");
              } /*if*/
            gl.glShaderSource(id, Source);
            CheckError("setting shader source");
            gl.glCompileShader(id);
            CheckError("compiling shader source");
            int[] Status = new int[1];
            gl.glGetShaderiv(id, gl.GL_COMPILE_STATUS, Status, 0);
            if (Status[0] == gl.GL_FALSE)
              {
                System.err.println
                  (
                        "GLUseful failed to compile shader source:\n"
                    +
                        Source
                    +
                        "\n"
                  ); /* debug */
                throw new RuntimeException
                  (
                        "Error compiling shader: "
                    +
                        GetShaderInfoLog(id)
                  );
              } /*if*/
          } /*Shader*/

        public void Release()
          {
            gl.glDeleteShader(id);
          } /*Release*/

      } /*Shader*/

    public static class Program
      {
        public final int id;
        private final Shader VertexShader, FragmentShader;
        private final boolean OwnShaders;

        public Program
          (
            Shader VertexShader,
            Shader FragmentShader,
            boolean OwnShaders /* call Release on shaders on my own Release */
          )
          {
            id = gl.glCreateProgram();
            if (id == 0)
              {
                ThrowError("creating program");
              } /*if*/
            this.VertexShader = VertexShader;
            this.FragmentShader = FragmentShader;
            this.OwnShaders = OwnShaders;
            gl.glAttachShader(id, VertexShader.id);
            CheckError("attaching vertex shader");
            gl.glAttachShader(id, FragmentShader.id);
            CheckError("attaching fragment shader");
            gl.glLinkProgram(id);
            int[] Status = new int[1];
            gl.glGetProgramiv(id, gl.GL_LINK_STATUS, Status, 0);
            if (Status[0] == gl.GL_FALSE)
              {
                throw new RuntimeException
                  (
                        "Error linking program: "
                    +
                        gl.glGetProgramInfoLog(id)
                  );
              } /*if*/
          } /*Program*/

        public Program
          (
            String VertexShaderSource,
            String FragmentShaderSource
          )
          {
            this
              (
                new Shader(gl.GL_VERTEX_SHADER, VertexShaderSource),
                new Shader(gl.GL_FRAGMENT_SHADER, FragmentShaderSource),
                true
              );
          } /*Program*/

        public void Validate()
          {
            gl.glValidateProgram(id);
            int[] Status = new int[1];
            gl.glGetProgramiv(id, gl.GL_VALIDATE_STATUS, Status, 0);
            if (Status[0] == gl.GL_FALSE)
              {
                throw new RuntimeException
                  (
                        "Error validating program: "
                    +
                        gl.glGetProgramInfoLog(id)
                  );
              } /*if*/
          } /*Validate*/

        public int GetUniform
          (
            String Name,
            boolean MustExist
          )
          {
            final int Result = gl.glGetUniformLocation(id, Name);
            if (MustExist && Result < 0)
              {
                ThrowError("getting uniform location");
              } /*if*/
            return Result;
          } /*GetUniform*/

        public int GetAttrib
          (
            String Name,
            boolean MustExist
          )
          {
            final int Result = gl.glGetAttribLocation(id, Name);
            if (MustExist && Result < 0)
              {
                ThrowError("getting attribute location");
              } /*if*/
            return Result;
          } /*GetAttrib*/

        public void Use()
          {
            gl.glUseProgram(id);
          } /*Use*/

        public void Unuse()
          {
            gl.glUseProgram(0);
          } /*Unuse*/

        public void Release()
          {
            gl.glDetachShader(id, VertexShader.id);
            gl.glDetachShader(id, FragmentShader.id);
            if (OwnShaders)
              {
                VertexShader.Release();
                FragmentShader.Release();
              } /*if*/
            gl.glDeleteProgram(id);
          } /*Release*/

      } /*Program*/

  } /*GLUseful*/
