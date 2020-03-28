#!/usr/bin/env bash
set -e

repository="rutledgepaulv"
application="narky-blog-io"
rm -rf target
full_commit=$(git rev-parse HEAD)
branch=$(git rev-parse --abbrev-ref HEAD)
abbrev_commit=${full_commit:0:8}
timestamp=$(date +%s)
full_image_name="${repository}/${application}:${timestamp}-${abbrev_commit}"
latest_name="${repository}/${application}:latest-${branch//\//-}"

echo "Building docker image with name [${full_image_name}]"
docker build \
  --build-arg USERNAME="${DATOMIC_REPOSITORY_USERNAME}" \
  --build-arg PASSWORD="${DATOMIC_REPOSITORY_PASSWORD}" \
  -t ${full_image_name} \
  -t ${latest_name} \
  .

echo "Authenticating to docker hub"
echo "$DOCKERHUB_PASS" | docker login -u "$DOCKERHUB_USER" --password-stdin

echo "Pushing docker images"
docker push ${full_image_name}
docker push ${latest_name}

echo "Generating kubernetes manifests"
replacements='${FOLDER},${IMAGE},${COMMIT}'
export IMAGE=${full_image_name}
export COMMIT=${abbrev_commit}
for path in kubernetes/*; do
  export FOLDER="${path##*/}"
  rm -rf target/${FOLDER}
  mkdir -p target/${FOLDER}
  for k8s_file in ${path}/*; do
    if [[ -f ${k8s_file} ]]; then
      filename=$(basename ${k8s_file})
      # apply templating to yaml files
      if [ ${filename: -5} = ".yaml" ]; then
        envsubst ${replacements} <"${k8s_file}" >target/${FOLDER}/${filename}
      fi
    fi
  done
done
