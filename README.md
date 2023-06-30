Java Raw Image Decoder
=

The original project which inspired this work is [DCRAW](https://dechifro.org/dcraw/) by Dave Coffin.  Please pay him a visit; his work is a great inspiration of contributing hard-earned knowledge.  Through a short email, Dave pointed me toward Huffman encodings--which enabled me to understand his code, and thus ultimately made my port possible.

This code was first written back in ~2002, but finally uploaded to GitHub in 2012.

This project features a Java raw decoder with a UI to change image gamma.  Raw images are lossless, generally high-bit-depth files from digital cameras.  They retain all the sensor information without lossy compression (e.g., JPEG).  This port isn't a finished project; it's just an example of a Java port of some awesome-yet-gnarly C code (which does the hard work of decoding the custom raw formats, and I've only verified my port against my own Canon CRW and CR2 images, even though Dave's original work decodes dozens of formats.

There are interesting parts of this code, which isn't the decoding port, that tries to show some object-oriented but low-level image-handling.  I happen to like the various iterators that traverse the image (in a way similar to how a convolution kernel processes images), in the handling of the raw-image-data.  Some considerations in its design included:

* Choosing an in-memory data format which would benefit from hardware acceleration
* Using ICC color-management through the use of Swing libraries
* Abstraction of the Bayer sensor mosaic filter
* Demo'ing the image-handling with a Swing component that visualizes the image
* Demo'ing some image post-processing with a little gamma adjuster.

I dropped work on the project as Apple and Adobe simultaneously released their digital asset management systems in the form of Aperture and Lightroom.  This project was still incredibly exciting, and filled with technical challenges.  It is in a state of disrepair, (last attended to back in prob 2007)!, due to then-abrupt RL demands; sometime in the distance future, I would like to get the code compiling again; if only to see it work myself.

Thanks, Dave.
