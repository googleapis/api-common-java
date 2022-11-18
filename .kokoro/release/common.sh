#!/bin/bash
# Copyright 2019 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -eo pipefail

# Get secrets from keystore and set and environment variables
setup_environment_secrets() {
  export GPG_PASSPHRASE=$(cat ${KOKORO_KEYSTORE_DIR}/70247_maven-gpg-passphrase)
  export GPG_TTY=$(tty)
  export GPG_HOMEDIR=/gpg
  mkdir $GPG_HOMEDIR
  mv ${KOKORO_KEYSTORE_DIR}/70247_maven-gpg-pubkeyring $GPG_HOMEDIR/pubring.gpg
  mv ${KOKORO_KEYSTORE_DIR}/70247_maven-gpg-keyring $GPG_HOMEDIR/secring.gpg
  export GPG_KEY_ID=$(echo -n $(gpg --with-colons ${GPG_HOMEDIR}/pubring.gpg | awk -F':' '/pub/{ print $5 }'))
  export SONATYPE_USERNAME=$(cat ${KOKORO_KEYSTORE_DIR}/70247_sonatype-credentials | cut -f1 -d'|')
  export SONATYPE_PASSWORD=$(cat ${KOKORO_KEYSTORE_DIR}/70247_sonatype-credentials | cut -f2 -d'|')
}

create_gradle_properties_file() {
  echo "
signing.gnupg.executable=gpg
signing.gnupg.homeDir=${GPG_HOMEDIR}
signing.gnupg.keyName=${GPG_KEY_ID}
signing.gnupg.passphrase=${GPG_PASSPHRASE}
ossrhUsername=${SONATYPE_USERNAME}
ossrhPassword=${SONATYPE_PASSWORD}" > $1
}
