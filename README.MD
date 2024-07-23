<h1>WIP</h1>
<h1>Mockito to mockk migration tool</h1>

NOTE! Please review your code after migration, this is tool was developed to help with conversion to mockk only.

TODO

InOrder,
InSequence

1. Add more tests coverages
2. Add main function which takes path param to file
3. **Optional** Convert into gradle plugin?-> Release?
4. Update Readme

Known Issues

1. Parser may fail if function or param name contains escape chars
2. Unnecessary spaces may break the parser - format the code before running it
3. For ArgumentCaptures, it only do single value conversion, does do pair.
4. Since ArgumentCapture and Slot differ in functionality, warning is added to every captured value