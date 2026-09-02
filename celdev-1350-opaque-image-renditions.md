# CELDEV-1350: Avoid unnecessary ARGB image renditions

## Goal

Implement [CELDEV-1350](https://synjira.atlassian.net/browse/CELDEV-1350) so opaque resized and filtered images use RGB buffers and produce PNGs without an alpha channel, while genuinely transparent inputs or backgrounds keep their alpha data.

## Handoff context

- Repository: `/home/fpichler/git/celements-image`, currently clean on `dev` when this plan was written.
- The cached-download flow is `ComputeImageCommand.computeImage` → `GenerateThumbnail.createThumbnail` → deprecated `GenerateThumbnail.encodeImage`; without an output override, JPEG input is intentionally encoded as PNG.
- `GenerateThumbnail` unconditionally allocates `TYPE_INT_ARGB` for lower-bound framing, background padding, and final rendering. The final allocation in `convertImageToBufferedImage` is what makes an ordinary opaque JPEG rendition an RGBA PNG.
- `ComputeImageCommand` separately allocates an ARGB staging buffer for kernel filters. `CropImage` only returns a subimage and `DecodeImageCommand.convertCmykToRgb` already uses `TYPE_INT_RGB`.
- Do not change output-format selection, add JPEG recompression, or implement WebP here; WebP remains [CELDEV-1267](https://synjira.atlassian.net/browse/CELDEV-1267).

## Tasks

- [ ] Add failing regression tests in `component/src/test/java/com/celements/photo/image/GenerateThumbnailTest.java` for an opaque `TYPE_INT_RGB` source: resize it through `createThumbnail` with JPEG input type/no override, decode the emitted PNG, and assert both `getColorModel().hasAlpha() == false` and unchanged dimensions/pixels. → Verify: the new test fails because the current output is RGBA.
- [ ] Add transparency regressions in `GenerateThumbnailTest`: a `TYPE_INT_ARGB` source with a transparent pixel and no background, plus an RGB source padded with a translucent `defaultBg`, must emit PNGs with alpha and preserve representative alpha values; an opaque `defaultBg` must flatten transparent source pixels and emit RGB. → Verify: the tests distinguish required transparency from the opaque fast path.
- [ ] In `component/src/main/java/com/celements/photo/image/GenerateThumbnail.java`, derive the destination buffer type once from the original `BufferedImage` and requested background: without a background, preserve alpha only when the source color model has alpha; with a background, use `TYPE_INT_ARGB` only when `defaultBg.getAlpha() < 255`, because an opaque fill flattens source transparency. Otherwise use `TYPE_INT_RGB`. Pass that decision into `convertImageToBufferedImage` and use it for the lower-bound, padded-background, and final buffers. Dispose every touched `Graphics2D` instance. → Verify: no unconditional `TYPE_INT_ARGB` allocation remains in this class and all new tests pass.
- [ ] In `component/src/main/java/com/celements/photo/plugin/cmd/ComputeImageCommand.java`, make the kernel-filter staging buffer follow the decoded source color model (`ARGB` only when it has alpha, otherwise `RGB`) without changing border replication or convolution behavior. Add a focused package-level selector test in `ComputeImageCommandTest` if extracting the decision makes it directly testable. → Verify: an `rg "TYPE_INT_ARGB"` audit shows every remaining use is conditional on transparency.
- [ ] Re-run existing thumbnail cases for ordinary resize, lower-bound crop, padded background, watermark, and copyright behavior; keep the encoder/cache contract unchanged so cache keys and output-format handling do not change. → Verify: existing assertions remain green and transparent pixels are not flattened.
- [ ] Review the diff for Celements conventions: explicit imports, no new fully qualified Java type names, no wildcard non-static imports, no empty lines inside methods, and no lint/Sonar issues. → Verify: `git diff --check` succeeds and the diff is limited to the two production classes and their focused tests.
- [ ] **Verification (last):** run `mvn -f component/pom.xml -Dtest=GenerateThumbnailTest,ComputeImageCommandTest test`, then `mvn -f component/pom.xml test`. After deployment with a cleared derived-image cache, request the Jira example once to regenerate and once from cache; confirm the generated PNG is RGB/no alpha and record its byte size and cached response time. → Expected: both Maven runs pass, transparent fixtures remain RGBA, and the opaque 800×533 rendition is materially smaller than the previous approximately 839 KB RGBA PNG.

## Done when

- [ ] All CELDEV-1350 acceptance criteria are covered by automated tests, opaque rendition PNGs have no alpha channel, transparency remains intact where requested, and no encoding-format behavior from CELDEV-1267 is bundled into the change.

## Suggested skills

- `executing-plans` to work through this file task by task.
- `test-driven-development` to establish the opaque/transparent regressions before production changes.
- `verification-before-completion` before declaring the implementation ready.
