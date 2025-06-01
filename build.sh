#!/usr/bin/env bash

set -e

# build the jar file into /public/jar/binary.jar
# also, copy the `resources/` directory into /public/jar/resources/

echo "Java version:"
java -version

# Clean previous build
rm -rf public/jar

# Create directories if they don't exist
mkdir -p public/jar/classes
mkdir -p public/jar/resources

# Copy resources into the jar directory
cp -r resources/* public/jar/resources/

# BUILD STEPS

# Create manifest
MANIFEST_FILE=public/jar/manifest.txt
echo "Main-Class: Main.Main" > $MANIFEST_FILE

# Compile all Java files in src/ to the classes directory
javac -sourcepath src -d public/jar/classes $(find src -name "*.java")

# Package the classes into a jar with the manifest (with the manifest file)
jar cfm public/jar/binary.jar $MANIFEST_FILE -C public/jar/classes .

# Clean up
rm -rf public/jar/classes
rm -f $MANIFEST_FILE

echo "Build complete: public/jar/binary.jar"
