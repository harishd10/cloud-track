Track Cloud Systems
===================

A visual exploration framework to track and query cloud systems.

Reference:
Harish Doraiswamy, Vijay Natarajan, and Ravi S. Nanjundiah, "An Exploration framework to identify and track movement of cloud systems", IEEE Transactions on Visualization and Computer Graphics (IEEE SciVis ’13), 19 (12), 2013, 2896--2905.


The code is an Eclipse peoject. To run the framework, you also need python with opencv and numpy support. The data sets supported are:

[1] CPC-merged IR brightness temperature data. http://mirador.gsfc.nasa.gov/collections/MERG__001.shtml
[2] Nasa trmm repository. http://trmm.gsfc.nasa.gov/.

To run the framework, you need to execute the following sub-programs in the given order:

(1) Java program: hope.it.works.data.IRDataLittleEndian (for data from [1]) or TrmmNCDataLittleEndian (for data from [2]). Note that you need to specify (in the code) the region of interest, and you can also optionally specify if you want to sub-sample the data. This preprocesses the data creates files that are read by the framework.

(2) Python program: createFlow.py (or createFlowTrmm.py) - This creates a flow field using the preprocessed data. Note again that you need to specify the resolution, extent, and other options (data location etc.) in the code.

(3) Java program: vgl.iisc.volray.MapViewer - This reads a property file (sample available in input/ directory). 

Options for the viewer (3) can be specified by pressing "c" and entering a command. I'll soon make a list of commands supported.
For now, you can check the executeComment() function in OffRendererTime.java 



