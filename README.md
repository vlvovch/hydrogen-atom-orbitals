<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" align="right" />

# Hydrogen Atom Orbitals <a href='https://play.google.com/store/apps/details?id=com.vlvolad.hydrogenatom'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height=60px/></a>


This repository contains the source code of the [Hydrogen Atom Orbitals app for Android](https://play.google.com/store/apps/details?id=com.vlvolad.hydrogenatom)





The code is designed to be used via Android Studio with Gradle.

## Short description of the app 


The app visualizes the electron orbitals of the hydrogen atom in 3D by using OpenGL.
More specifically, the hypersurfaces of constant spatial electron probability density of the different orbitals are drawn, exploiting the known exact solution of the Schroedinger equation for hydrogen atom.


The quantum numbers *n*, *l*, and *m*, the probability *P* to find electron inside the orbital, and the discretization level can all be varied.
Choice between complex and real (in azimuthal angle) basis of wave functions is possible.


The visualization is performed by dynamically generating the hypersurfaces with the [Marching Cubes](http://paulbourke.net/geometry/polygonise/) algorithm. Orbitals can be interactively zoomed and rotated.
