![VALDA 3D Engine Banner](assets/VALDA_FINAL.png)

### **What is it?**
**VALDA** is a multi-threaded, tile-based CPU software rasterizer written in Java built from scratch without external graphics APIs. It provides custom math and rendering classes to transform 3D geometry, apply lighting values, sample textures, and render frames on screen.
<br><br>
### **Motive**
The VALDA engine grew out of hands-on studying in linear algebra, computer graphics, and system design-reasearching how mathematical transforms, clipping, and rasterizing can turn raw numbers into real-time rendered visuals. Working on this project bridged the gap between abstract math and low-level rendering behavior.
<br><br>

## Mesh Generation

VALDA is capable of generating various geometric shapes including cubes, pyramids, spheres, and custom polygons. The engine requires a list of vertices and applicable indices in order to render the mesh.

| Parameter | Shader | Wireframe |
| :--- | :---: | :---: |
| **Render** | <img src="assets/MESHES_DEMO.png" alt="Mesh Generation" width="480" /> | <img src="assets/WIREFRAME_DEMO.png" alt="Wireframed Meshes" width="480" /> |

<br><br>
## Lighting

| Parameter | Point | Spot | Directional |
| :--- | :---: | :---: | :---:|
| **Render** | <img src="assets/POINTLIGHT_DEMO.png" alt="Point Light" width="260" /> | <img src="assets/SPOTLIGHT_DEMO.png" alt="Spotlight" width="260" /> | <img src="assets/DIRECTIONAL_DEMO.png" alt="Directional Light" width="260" /> |
| **Intensity** | 1 | 1 | 1 |
| **Linear** | 0.05 | 0.05 | - |
| **Quadratic** | 0.05 | 0.05 | - |
| **Inner Cutoff** | - | 0.8 | - |
| **Outer Cutoff** | - | 0.7 | - |
| **Yaw** | - | 0 | 0 |
| **Pitch** | - | 0 | -90 |

<br><br>
## Texture Sampling

VALDA utilizes UV texture mapping and barycentric coordinates to assign color to an interpolated pixel belonging to an entity. Images below use bilinear texture filtering.

| Parameter | With Lighting | No Lighting |
| :--- | :---: | :---: | 
| **Render** | <img src="assets/TEXTURE_SAMPLING_DEMO.png" alt="Texture with light" width="480" /> | <img src="assets/TEXTURE_SAMPLING_NO_LIGHT_DEMO.png" alt="Texture no light" width="480" /> |

<br><br>

## Engine Architecture

<p align"center">
  <img src="assets/class_diagram.png" alt="VALDA Engine Class Architecture Diagram" width = "1080"/>
</p>

<br><br>

## Memory Efficiency and Data Layout

To minimize garbage collector overhead, the engine uses flat primitive arrays and pre-allocated buffers: 

* **Contiguous Stride Buffers:** Vertex values are packed into flat double[] arrays to improve CPU cache locality and reduce cache misses (Example: INITIAL_MESH_STRIDE = 6 for [x, y, z, w, u, v], expanding to 12 values per vertex once normal and tangent vectors are baked in).

<br>

* **Global Scene Buffers and Offsets:** The Scene class packs entity geometry values into shared global arrays and assigns offsets to each Entity, avoiding repeated array allocations during rendering.

<br>

* **Tile Buffers:** The screen is divided into partitioned tiles, each owning its own colorBuffer and depthBuffer. Worker threads rasterize assigned tiles in parallel without sharing mutable pixel buffers.

<br><br>

**Specifications**: 
* Requires Java extension installation (such as VS Code Java extension).

<br>

**Complie Instructions**: 
* javac -d out src/*.java

<br>

**Run Instruction**: 
* java -cp out Main
