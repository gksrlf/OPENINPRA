Android 3D Model Viewer
=======================

![codeship badge](https://codeship.com/projects/52cf9560-deb2-0134-4203-2aaddef843aa/status?branch=master)

This is a demo of OpenGL ES 2.0.
It is an android application with a 3D engine that can load Wavefront OBJ, STL, DAE & glTF files.
The application is based on andresoviedo's project which can be found [here](https://github.com/andresoviedo/android-3D-model-viewer) with an additional function of loading and rendering glTF format.
<br>
The purpose of this application is to learn and share how to draw using OpenGLES and Android. As this is my first android app, it is highly probable that there are bugs; but I will try to continue improving the app and adding more features.

* Wafefront format (OBJ): https://en.wikipedia.org/wiki/Wavefront_.obj_file
* STereoLithography format (STL): https://en.wikipedia.org/wiki/STL_(file_format)
* Collada format (DAE): https://en.wikipedia.org/wiki/COLLADA
* glTF format (gltf): https://github.com/KhronosGroup/glTF


News (12/08/2019)
=================

* New: Add partially support for glTF models

Supported Feature for glTF Models
==================
> * Scene
>   
>   * [x]  choosing scenes
> * Node
>   
>   * [x]  Transformations (matrix, TRS)
>   * [x]  children
>   * [x]  mesh
>   * [ ]  camera 
>       * currently only use a single camera and force the object at center for easier view
>   * [ ]  skin
>   * [ ]  weights
> * Mesh
>   
>   * [x]  primitives
>   * [ ]  weights
> * Primitive
>   
>   * [x]  Attributes
>     
>     * [x]  positions, normals, tangents, 2 tex coord sets, 1 color set, joints, weights
>     * [x]  all tex coord formats
>     * [x]  TEX_COORD_1
>     * [x]  all color formats
>     * [x]  COLOR_0
>   * [x]  Indices
>   * [x]  No Indices (-> `glDrawArrays()`)
>   * [x]  Material
>   * Mode
>     
>     * [x]  Triangles
>     * [x]  Others: Points, Lines, LineLoop, LineStrip, TriangleStrip, TriangleFan
>   * [ ]  targets
> * Material
>   
>   * [x]  pbrMetallicRoughness
>     
>     * [x]  base color (factor + texture)
>     * [ ]  full PBR lighting
>   * [ ]  normalTexture
>   * [ ]  occlusionTexture
>   * [x]  emissiveTexture  + emissiveFactor
>   * [ ]  alphaMode + alphaCutoff*
>     (works, but not always correct - transparency/depth sorting is missing)
>   * [x]  doubleSided
> * Texture
>   
>   * [x]  Sampler
>   * [x]  Image
>   * [x]  textureInfo (texCoord set index)
> * [ ]  Animation
> * [ ]  Skin

Demo
====

Checkout this to see the features of the application: https://youtu.be/AB8fHq_CkpU


Android Market
==============

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" width="323" height="125">](https://play.google.com/store/apps/details?id=org.andresoviedo.dddmodel2)


Notice
======

* Collada support is limited. Collada renderer currently supports a maximum of 60 bones.
* In order to see models in 3D virtual reality, you need red-cyan and/or VR glasses
* If you have any issue in general,please open an issue and attach model if possible, specifying Android version and Device model.  


About
=====

Load 3D models and see how to do it with this open source code application.

The main purpose of this app is to show how to draw in android using the OpenGL 2.0 by sharing the source code.
So please, don't expect this application to be much richer or nicer than the ones already published in the app store,
but at least it's opened to anyone who wants to contribute or don't want to start a similar project from scratch.

As this is my first android app and Im still learning the OpenGL 2.0 language, it is highly probable that there are bugs;
but I will try to continue improving the app and adding more features. So please send me your comments, suggestions or
complains by opening an [issue](https://github.com/andresoviedo/android-3D-model-viewer/issues) or email me to andresoviedo@gmail.com.

The app comes with some included 3D models that were taken for free from Internet.


Whats next
==========

* Chromecast support
* 3D without glasses
* Augmented reality
* Collada:  Show bind pose, key frames and variate speed
* glTF: Add animation support
* glTF: Apply PBR material
* Add support to Android API below level 24
* Clean up code


Features
========

  - Supports >= API Level 24
  - OpenGL ES 2.0 API
  - Formats: OBJ (wavefront), STL (STereoLithography), DAE (Collada-BETA), gltf (glTF)
  - calculation of normals
  - transformations: scaling, rotation, translation
  - colors
  - textures
  - lighting
  - wireframe & points mode
  - bounding box drawing
  - object selection
  - camera support
    - tap to select object
    - drag to move camera
    - rotate with 2 fingers to rotate camera
    - pinch & spread to zoom in/out the camera
  - skeletal animations
  - ray collision detection
  - stereoscopic 3D
  - other:
    - texture loader
    - lightweight: only 1 Megabyte


Try it
======

You can install the application in either of these ways:
  <br>Without glTF support:
  * Play Store:  https://play.google.com/store/apps/details?id=org.andresoviedo.dddmodel2
  * APK: [app-release.apk](app/build/outputs/apk/release/app-release.apk)

  With glTF support:
  * clone the repository, compile with gradle and install with adb

```
    export ANDROID_HOME=/home/$USER/Android/Sdk
    ./gradlew assembleDebug
    adb install -r app/build/outputs/apk/app-debug.apk
    adb shell am start -n org.andresoviedo.dddmodel2/org.andresoviedo.app.model3D.MainActivity
```
* clone the repository, build and run with android studio

Open the application. You should see a menu. From there you can load models
Once the model is rendered, pinch and rotate to see the 3D scene from another perspective.


Screenshots
===========

![Screenshot1](screenshots/screenshot1.png)
![Screenshot2](screenshots/screenshot2.png)
![Screenshot3](screenshots/screenshot3.png)
![Screenshot4](screenshots/screenshot4.png)
![Screenshot5](screenshots/screenshot5.png)
![cowboy.gif](screenshots/cowboy.gif)
![stormtrooper.gif](screenshots/stormtrooper.gif)
![Screenshot6](screenshots/screenshot6-3d.png)


Emulator
========

You can run application in an emulator

    // install some file provider (i.e. es file explorer)
    adb devices -l
    adb -s emulator-5554 install .\com.estrongs.android.pop_4.0.3.4-250_minAPI8(armeabi,x86)(nodpi).apk
    // push some files to test file loading
    adb -s emulator-5554 push .\app\src\main\assets\models /sdcard/download


Documentation
=============

https://github.com/andresoviedo/android-3D-model-viewer/wiki


Final Notes
===========

You are free to use this program while you keep this file and the authoring comments in the code.
Any comments and suggestions are welcome.


Contact
=======

http://www.andresoviedo.org


Donations
=========

[<img src="https://www.paypalobjects.com/webstatic/en_US/i/btn/png/btn_donate_92x26.png">](https://www.paypal.me/andresoviedo)


Marketing
=========

If you want to buy 3D glasses on Amazon, thank you for clicking on following links as it may help supporting 
this project:

[<img src="https://raw.githubusercontent.com/andresoviedo/android-3D-model-viewer/master/market/glasses-3d.jpg">](https://amzn.to/2E8LhxC)
[<img src="https://raw.githubusercontent.com/andresoviedo/android-3D-model-viewer/master/market/cardboard-3d.jpg">](https://amzn.to/2E8M1Tq)



ChangeLog
=========

(f) fixed, (i) improved, (n) new feature

- 2.5.1 (20/05/2019)
  - (f) wavefront loader fixed for faces point to negative indices
- 2.5.0 (19/05/2019)
  - (n) new blending toggle
  - (n) new color toggle
  - (i) engine refactoring: externalized shaders
  - (i) engine improved: fixed bugs and removed classes
- 2.4.0 (16/05/2019)
  - (n) stereoscopic rendering: anaglyph + cardboard
- 2.3.0 (27/09/2018)
  - (n) Externalized 3d engine into android library module
  - (n) Wiki initial documentation
- 2.2.0 (11/09/2018)
  - (n) Load models from app repository
  - (i) Reduced app size to only 1 Megabyte
- 2.1.0 (07/09/2018)
  - (n) Skeleton Animation
  - (n) File chooser to load files from any where
  - (f) Collada Animator fixed (INV_BIND_MATRIX, bind_shape_matrix)
  - (f) Collada Animator Performance improved
  - (f) Application refactoring (ContentUtils, Loaders, etc)
  - (f) Several bugs fixed
- 2.0.4 (22/12/2017)
  - (n) Implemented face collision detection algorithm: ray-triangle + octree
- 2.0.3 (21/12/2017)
  - (i) Improved collision detection algorithm (ray-aabb) for selecting objects
  - (i) BoundingBox code cleanup
- 2.0.2 (17/12/2017)
  - (f) Collada XML parser is now android's XmlPullParser
  - (f) Animation engine frame times improved
  - (n) Camera now moves smoothly
- 2.0.1 (08/12/2017)
  - (f) Multiple Collada parser fixes
  - (f) Camera now can look inside objects
- 2.0.0 (24/11/2017)
  - (n) Support for collada files with skeletal animations :)
- 1.4.1 (21/11/2017)
  - (f) #29: Crash loading obj with only vertex info
- 1.4.0 (19/11/2017)
  - (f) #28: Load texture available for any model having texture coordinates
- 1.3.1 (23/04/2017)
  - (f) #18: Removed asReadOnlyBuffer() because it is causing IndexOutOfBounds on Android 7
- 1.3.0 (17/04/2017)
  - (n) #17: Added support for STL files
  - (n) #17: Asynchronous building of model so the build rendering is previewed
  - (f) #17: Added Toasts to buttons to show current state
- 1.2.10 (16/04/2017)
  - (f) #16: Immersive mode is now configurable in the ModelActivity Intent: b.putString("immersiveMode", "false");
  - (f) #16: Background color configurable in the ModelActivity Intent: b.putString("backgroundColor", "0 0 0 1");
  - (f) #16: Fixed vertex normals generation (vertices were missing)
  - (f) #16: Scaling is now implemented in the ModelView Matrix with Object3DData.setScale(float[])
  - (f) #16: Wireframe generation is now using the source data
  - (n) #16: Implemented Point Drawing, like wireframe mode but only the points are drawn
  - (f) #16: Removed trailing slash from parameter "assetDir"
  - (f) #16: Access to ByteBuffers made absolute so there are thread safe (future fixes need this)
- 1.2.9 (11/04/2017)
  - (f) #15: Toggle rotating light
  - (f) #15: Wireframe with textures and colors
- 1.2.8 (10/04/2017)
  - (f) Fixed #14: Camera movement improved. Only 1 rotation vector is used + space bounds set
- 1.2.8 (04/04/2017)
  - (f) Fixed #13: parsing of vertices with multiple spaces
  - (i) Improved error handling on loading task
  - (i) Vertices are defaulted to (0,0,0) if parsing fails
- 1.2.7 (03/04/2017)
  - (i) Removed commons-lang3 dependency
- 1.2.6 (02/04/2017)
  - (f) Fixed #12. Drawing the wireframe using GL_LINES and the index buffer (drawElements)
- 1.2.5 (01/04/2017)
  - (f) Fixed #10. Map faces to texture only when using the only loaded texture
  - (f) Fixed #11. Generation of missing vertex normals
- 1.2.4 (30/03/2017)
  - (f) Fixed #5. Memory performance optimization
- 1.2.3 (27/03/2017)
  - (f) Fixed #1. Cpu performance optimization
- 1.2.2 (25/03/2017)
  - (f) Fixed #9. IOOBE loading face normals when faces had no texture or normals
- 1.2.1 (27/02/2017)
  - (f) Fixed loading external files issue #6
  - (i) Project moved to gradle
- 1.2.0 (06/04/2016)
  - (n) Implemented selection of objects
- 1.1.0 (30/03/2016)
  - (n) Implemented lighting & toggle textures & lights
  - (i) Refactoring of 3DObjectImpl
- 1.0.0 (27/03/2016)
  - (n) First release in Google Play Android Market
