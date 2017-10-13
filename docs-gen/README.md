These projects are mirrors of sample to convert `README.md` into Paradox documentation.

You can test the generation by following these steps:

```
cd docs-gen
sbt paradox
open */target/paradox/site/main/index.html
```

The last line is macOS specific, and opens all of the example project README files in separate browser tabs.

You can replace `open` with a suitable command for your OS, and replace `*` with a specific sub-project if you only need to review one page.
