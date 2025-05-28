--------------------------------------------------------------------------------
:page/title Proper URLs for your SPA on S3 behind Cloudfront
:tech-blog/published #time/ldt "2020-10-10T11:00"
:tech-blog/tags [:tag/aws]
:open-graph/description

How to use Lambda@Edge to give JavaScript frontends/SPAs on AWS S3 behind
Cloudfront proper URLs even when all you have is a single HTML file and some
JavaScript.

:tech-blog/description

In this post I'll show you how to use Lambda@Edge to give JavaScript frontends
("Single Page Applications") on AWS S3 behind Cloudfront proper URLs even when
all you have is a single HTML file and some JavaScript.

--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title Proper URLs for your SPA on S3 behind Cloudfront
:section/body

Running static frontends on AWS S3 behind Cloudfront is easy, cheap, and close
to maintenance-free. However, out of the box it only gives you a 1:1 mapping
between files and URLs. Not to worry though,
[Lambda@Edge](https://aws.amazon.com/lambda/edge/), AWS' Lambda function
offering for Cloudfront, allows for various transformations, such as URL
rewrites.

--------------------------------------------------------------------------------
:section/title The problem
:section/body

A single-page application consists of:

- `app.js` - A minified bundle of your entire app
- `styles.css` - A minified bundle of your app styles
- `index.html` - Loads the sources and "hosts" the app

Sync this to S3 and put Cloudfront in front of it, and you have a pretty sturdy
setup. So what's the problem? Well:

- Visit your SPA in the browser, e.g. `https://mybananas.com/`
- Click on "login". Via `pushState`, the browser says you're now on
  `https://mybananas.com/login`
- Hit refresh
- Shed a tear for the 404 page you are now staring at

The problem is that there is no file called `login` or `login/index.html` in the
S3 bucket, which is what Cloudfront is telling you with the 404. You might think
that this problem is trivially solved by changing `/login` to `/#login` and
you'd be right - if you hate URLs. If you want real URLs, read on.

--------------------------------------------------------------------------------
:section/title The solution
:section/theme :dark1
:section/body

Cloudfront distributions have [four hooks for Lambda
functions](https://docs.aws.amazon.com/lambda/latest/dg/lambda-edge.html). The
viewer request hook allows the function to manipulate the request before
consulting the cache and/or the backend (S3 in our case). Specifically, we can
make sure any request for a "page" can be rewritten as a request for
`index.html`. This will result in the SPA being loaded, and if the app reads
`window.location` as part of its bootup, it will load on the correct page.

## How to recognize a page?

A "page" is a loose concept. You _could_ give all your pages `.html` suffixes
and check for it, but most people prefer their URLs to be free of file suffixes
and other noise. If we go this way, we could use this logic to recognize a page:
any URL that doesn't contain a file suffix is assumed to be a page:

```js
// url-rewrite-lambda.js

exports.handler = (event, context, callback) => {
  const request = event.Records[0].cf.request;

  if (!/\..+/.test(request.uri)) {
    request.uri = `/index.html`;
  }

  callback(null, request);
};
```

That's the whole Lambda function. If the URL doesn't contain a dot and some more
characters, change the request URL to `index.html`.

## Deploying the Lambda

There are many ways to deploy a Lambda function. Lambda@Edge is not a completely
separate service from AWS Lambda - you can upload the function just like you
upload regular Lambdas. What makes the function an "edge function" is when you
add a Cloudfront trigger to it. However you deploy the lambda, make sure to
deploy it to the `us-east-1` region, as this is the region Cloudfront will use
to sync the lambda to edge servers.

To provide you with a practical example, here is how I deploy these using
Terraform:

```
data "aws_iam_policy_document" "lambda" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type = "Service"
      identifiers = [
        "lambda.amazonaws.com",
        "edgelambda.amazonaws.com"
      ]
    }
  }
}

resource "aws_iam_role" "lambda_role" {
  name_prefix = "mybananas.com"
  assume_role_policy = "${data.aws_iam_policy_document.lambda.json}"
}

resource "aws_iam_role_policy_attachment" "lambda_exec" {
  role = "${aws_iam_role.lambda_role.name}"
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

data "archive_file" "rewrite" {
  type = "zip"
  output_path = "${path.module}/.zip/rewrite.zip"

  source {
    filename = "lambda.js"
    content = "${file("${path.module}/url-rewrite-lambda.js")}"
  }
}

resource "aws_lambda_function" "url_rewrite" {
  provider = "aws.us-east-1"
  function_name = "mybananas-com-url-rewrite"
  filename = "${data.archive_file.rewrite.output_path}"
  source_code_hash = "${data.archive_file.rewrite.output_base64sha256}"
  role = "${aws_iam_role.lambda_role.arn}"
  runtime = "nodejs10.x"
  handler = "lambda.handler"
  memory_size = 128
  timeout = 3
  publish = true
}

resource "aws_cloudfront_distribution" "s3_distribution" {
  # ...

  default_cache_behavior {
    # ...

    lambda_function_association {
      event_type = "viewer-request"
      lambda_arn = "${aws_lambda_function.url_rewrite.qualified_arn}"
      include_body = false
    }
  }
}
```

--------------------------------------------------------------------------------
:section/title Other uses for Edge Lambdas
:section/body

Edge Lambdas can be used as a light backend for mostly static sites hosted on
S3/Cloudfront. In addition to URL rewrites, I've used the viewer response hook
to add custom headers
([CSP](https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP),
[HSTS](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Strict-Transport-Security),
etc).

The viewer request hook can also be used to add authentication. The origin
response hook can be used to embellish files from S3 before sticking them in
Cloudfront's cache - e.g. inlining some API data in HTML files to bootstrap the
SPA, etc. Your imagination is the only limit.
