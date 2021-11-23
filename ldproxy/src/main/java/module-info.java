module nl.clariah.ldproxy {
  requires Saxon.HE;
  requires SaxonUtils;
  requires java.xml;
  requires java.desktop;
  requires nl.clariah.recipe;
  uses nl.clariah.recipe.Recipe;
  exports nl.clariah.ldproxy;
}
