# StoATT
Implementation of a structured mean time to first failure computation scheme for static fault trees based on the Tensor Train format.
Application of the TT-based approach for Generalized Stochastic Petri-Nets is in progress.

Usage:
The CLIMain class provides a command-line interface to the tool. Use the _calc_ command for MTFF calculation.
Use the -h option to see the help page with all the available options.
Example for computing the MTFF (-m 1, because we need the first moment of the failure time) of 
the dft described by galileofile.dft, using the AMEn-ALS solver with enrichment 4 and dampening factor 1e-5:
```
java -jar stoatt.jar sft calc -f galileofile.dft -m 1 -s AMEn-ALS --enrichment 4 --damp 1e-5
```