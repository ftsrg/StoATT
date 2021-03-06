# StoATT
Implementation of a structured mean time to first failure computation scheme for static fault trees based on the Tensor Train format.
Application of the TT-based approach for Generalized Stochastic Petri-Nets is in progress.

Usage:
The CLIMain class provides a command-line interface to the tool. Use the _calc_ command for MTFF calculation.
Use the -h option to see the help page with all the available options.
Example for computing the MTFF (-m 1, because we need the first moment of the failure time) of 
the dft described by galileofile.dft, using the AMEn-ALS solver with enrichment 4 and dampening factor 1e-5:

```
stoatt-sft calc -f galileofile.dft -m 1 -s AMEn-ALS --enrichment 4 --damp 1e-5
```

## A note on dependencies
The core module depends on the _delta_ decision diagram library. It is not open-source yet, so it is included
as a jar file dependency.

The gspn module depends on another library, _turnout-petrinet_, which is not open-source yet either. This library
is not included yet, but as the modules are separate gradle projects, the SFT tool can be compiled without any 
problems.

## Building the SFT tool from source
Run the ```:stoatt-sft:distTar``` or ```:stoatt-sft:distZip``` gradle task to create a tar or ZIP distribution, 
including an executable and all dependencies. The resulting artifact can be found in the 
```stoatt-sft/build/distributions``` directory.

## Paper information
The implementation is based on the algorithm described in the paper _D. Szekeres, K. Marussy, I. Majzik: Tensor-Based Reliability Analysis of Complex Static Fault Trees_.

The paper is available at [the EDCC 2021 conference website](https://edcc2021.iks.fraunhofer.de/program.html).

```
@inproceedings{szekeres2021tensor,
  title={Tensor-Based Reliability Analysis of Complex Static Fault Trees},
  author={Szekeres, D\'aniel and Marussy, Krist\'of and Majzik, Istv\'an},
  booktitle={European Dependable Computing Conference},
  pages={33--40},
  year={2021},
  organization={IEEE},
  ISBN="978-1-6654-3671-7"
}
```

## License
The project uses the Apache Public License v2.0.
