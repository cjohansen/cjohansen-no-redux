#!/bin/bash

set -ex

dist_id=$(cd infrastructure && terraform output -raw distribution_id)
bucket=$(cd infrastructure && terraform output -raw bucket_name)

cd target

aws s3 sync . s3://$bucket --cache-control max-age=31536000,public,immutable --exclude "*" --metadata-directive REPLACE --include "css/*" --include "fonts/*"
aws s3 sync . s3://$bucket --delete --exclude "css/*" --exclude "fonts/*"
aws cloudfront create-invalidation --distribution-id $dist_id --paths /index.html /
