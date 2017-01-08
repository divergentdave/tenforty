# tenforty

A library to assist in analyzing the US tax system.

## Usage

This library provides infrastructure to perform tax calculations. It is still under early development, and is being developed alongside two other Clojure projects that use it, [tenforty.tools](https://github.com/divergentdave/tenforty.tools) and [tenforty.webapp](https://github.com/divergentdave/tenforty.webapp).

## Installation

`tenforty` requires a working Java runtime and a Clojure runtime (by way of [Leiningen](https://leiningen.org/)). If your computer does not yet have Java, install it from your operating system's package manager or from https://java.com/. Next, install Leiningen according to the [installation instructions](https://leiningen.org/#install) for your operating system.

Once you have installed these dependencies, you can either check out this repository manually, or run the command below to check out all three `tenforty` projects and perform other development environment setup.

```
curl -o- https://raw.githubusercontent.com/divergentdave/tenforty/master/scripts/bootstrap | bash
```

## Object Model

The tenforty object model is built around `Line` objects, which correspond to lines on tax forms, worksheets, or other intermediate values described in instructions. There are input lines of different types (with numeric values, boolean values, or selections from proscribed options) and lines computed from formulas. Values of formula lines can be computed given values for their inputs. A list of which lines are used in a formula is stored with the line at compile time, so that data dependencies can be analyzed, displayed, or used in computation. Each line is identified by a Clojure keyword, typically prefixed with a Clojure namespace.

Multiple lines in the same form or related forms are collected in a `FormSubgraph` before computation. `FormSubgraph` objects can be merged together, either to build a model of a tax system that spans multiple files, or to modify a tax system by replacing certain lines with modified lines. When evaluating a formula line, definitions of parent lines will be looked up in the provided `FormSubgraph` object.

Sometimes, tax forms may contain a subsection that can be repeated an arbitrary number of times. For example, multiple W-2 forms may be attached to Form 1040. This situation is represented with repeating "groups". Each line belongs to a group, and FormSubgraph objects store information on all groups that their lines use. Groups can be nested within each other if needed, for example, if a form which can have multiple copies attached contains a table which can have any number of rows filled out. The default or top-level group is identified by `nil`, and other groups are identified by Clojure keywords.

Values for tax calculations come from `TaxSituation` objects. These objects are expected to return values of input cells (of the appropriate type) when provided with the keyword of a line. Some implementations are provided that use Clojure maps to look up values. One `TaxSituation` object only holds values from within one group. `TaxSituation` objects also return a list or vector of child `TaxSituation` objects for child repeating groups when provided with a group keyword.

Tax calculations can be performed by passing a `FormSubgraph` and `TaxSituation` to the `calculate` function, but if results from multiple lines will be used, it may be more efficient to build a `TenfortyContext` object first, and pass that to the `calculate` function. A `TenfortyContext` object is built from the combination of a `FormSubgraph` and a `TaxSituation`, and it additionaly caches calculated formula values, avoiding duplication of work.

## Tax Forms

This library includes select portions of the individual tax forms in the `tenforty.forms` namespace to get you started. However, this is practically doomed to be forever incomplete and/or out of date. Forms and formulas that aren't handled are usually stubbed out with input lines. Some exceptions in the instructions may not be properly handled. Don't rely on these files to be accurate.

## License

Copyright © 2016-2017 David Cook

Distributed under the GNU General Public License, version 2, as modified in `LICENSE`.
