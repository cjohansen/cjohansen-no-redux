--------------------------------------------------------------------------------
:page/title An umlaut too far
:tech-blog/published #time/ldt "2023-12-19T12:00"
:tech-blog/tags [:tag/java :tag/javascript :tag/unicode]
:tech-blog/description

Unicode is everywhere. But in some places, it's still safest to use the good old
ASCII character set, and in this post we'll look at Unicode normalization as a
way to get there.

:open-graph/description

Unicode normalization can help you make text URL-friendly, searchable, and more.
Learn why and how.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title An umlaut too far
:section/body

Unicode is everywhere. But in some places, it's still safest to use the good old
ASCII character set, and in this post we'll look at Unicode normalization as a
way to get there.

--------------------------------------------------------------------------------
:section/body

[På norsk](https://parenteser.mattilsynet.io/pull-requests/).

I recently wrote [a bit about search](/fulltext-search/), and how to achieve
[matching roughly what the user typed](/weighted-fulltext-search/). One thing I
haven't covered is how to help people find "el niño" when they type "el nino,"
or "café" when they type "cafe."

Characters like ñ and é can be a bit tricky to work with because neither is a
single character—there are several Unicode forms that produce the same visual
result. For example, ñ can be one of these two:

- **Latin Small Letter N with Tilde (ñ)**, Unicode code point `U+00F1`
- A combination of `U+006E` (**n**) and `U+0303` (**"Combining Tilde"**)

Fortunately, there's a solution to this mess: Unicode normalization.

Both Java and JavaScript have built-in tools to normalize Unicode to a given
form. There are several forms, but the one that helps us with accented letters
is `NFD` — Normalization Form Decomposition. This form breaks all composed
characters down into a base character and combining "diacritical marks" (such as
tilde and accent).

Strings can be normalized as follows in Java:

```java
import java.text.Normalizer;

String s1 = "El niño";
String s2 = Normalizer.normalize(s1, Normalizer.Form.NFD);
```

In JavaScript:

```js
var s1 = "El niño";
var s2 = s1.normalize("NFD")
```

You can print the result if you want, but you'll be disappointed—there's no
visible difference. Normalization only changes how the content is internally
represented. You might notice a difference in `.length` depending on the
original form. After normalization, `.length` will be 8, showing that the tilde
has been separated from the “n.”

Once that's done, the next step is to remove the code points from U+0300 to
U+036F:

```javascript
var s = "El niño";
s.normalize("NFD").replace(/[\u0300-\u036F]/, "");
//=> "El nino"
```

And there you have it! A handy little trick you can use next time you're
generating a URL slug, doing broad searches, or performing any kind of text
analysis.
