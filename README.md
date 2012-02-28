Java RAW-Image Decoder
=

This project features a Java RAW-image decoder with a UI to change gamma.  RAW-images are "raw" images from certain high-end digital cameras.  They are lossless formats which retain all the sensor information without lossy compression that would be suffered if output to a lossy compression format (e.g., JPEG).  It's not a finished project; it's just an example of an object-oriented port of a RAW image decoder written in C.  The original project which inspired my port is [DCRAW][http://www.cybercom.net/~dcoffin/dcraw/].  Please pay him a visit; his work is a great inspiration of contributing knowledge to the noosphere.

There are interesting parts of this code, particularly in the various iterators that traverse the image (similar to a convolution kernel), in the handling of the raw-image-data (in a format which would have benefitted from hardware acceleration), the move toward ICC color-management, and also in the abstractions of the Bayer sensor mosaic filter.  There is also Swing component that visualizes the image, and a little gamma adjuster.

I dropped work on the project as Apple and Adobe simultaneously released their digital asset management systems in the form of Aperture and Lightroom.  This project was still incredibly exciting, and filled with technical challenges.  It is in a state of disrepair at the moment, due to abrupt RL demands; sometime in the near future, I would like to get the code compiling again; if only to see it work myself.  It feels a bit 'dirty' to leave code in a half-baked(able) state...
