#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    openssl aes-256-cbc -K $encrypted_76d829fd1795_key -iv $encrypted_76d829fd1795_iv -in codesigning.asc.enc -out codesigning.asc -d
    gpg --fast-import cd/signingkey.asc
fi
