Track Cloud Systems
===================



A visual exploration framework to track and query cloud systems.

Reference:
Harish Doraiswamy, Vijay Natarajan, and Ravi S. Nanjundiah, "An Exploration framework to identify and track movement of cloud systems", IEEE Transactions on Visualization and Computer Graphics (IEEE SciVis ’13), 19 (12), 2013, 2896--2905.


The code is an Eclipse project. To run the framework, you also need python with OpenCV and numpy support. The data sets supported are:

[1] CPC-merged IR brightness temperature data. http://mirador.gsfc.nasa.gov/collections/MERG__001.shtml

[2] Nasa trmm repository. http://trmm.gsfc.nasa.gov/.

To run the framework, you need to execute the following sub-programs in the given order:

(1) Java program: hope.it.works.data.IRDataLittleEndian (for data from [1]) or TrmmNCDataLittleEndian (for data from [2]). Note that you need to specify (in the code) the region of interest, and you can also optionally specify if you want to sub-sample the data. This preprocesses the data creates files that are read by the framework.

(2) Python program: createFlow.py (or createFlowTrmm.py) - This creates a flow field using the preprocessed data. Note again that you need to specify the resolution, extent, and other options (data location etc.) in the code.

(3) Java program: vgl.iisc.volray.MapViewer - This reads a property file (sample available in input/ directory). 

You can select a cloud of interest but pressing control and mouse left click.

Options for the viewer (3) can be specified by pressing "c" and entering a command. 

The following commands are supported:

**direction <east,west,north,south>**-- finds clouds along the queries direction

**track <local,global>** -- tracks the local or global movement of the clouds.

**option <option> <value>**

The following options are supported:

*localGlyphSize (globalGlyphSize)* - size of the glyphs for visualizing local (global) movement.

*localLengthBefore (localLengthAfter)* - time to track and visualize the local movement of the cloud before (after) current time.
 		
*globalLengthBefore (globalLengthAfter)*  - time to track and visualize the global movement of the cloud before (after) current time.

*localSampleDensity (globalSampleDensity)* - sub-sample time steps (to make computation faster)

*time* - jump to a given time step (this is an integer representing the index from starting time)

**option color** -- This toggles highlighting the clouds with different colors.


Pressing "a" toggles animation mode

Pressing the arrow keys allows panning (mouse can also be used for this purpose)

Mouse right click is used for zooming in / out

There are possible other options hidden in the code. I will make an exhaustive list when I find time.

