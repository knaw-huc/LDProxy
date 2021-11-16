module nl.clariah.recipe {
  requires Saxon.HE;
  requires SaxonUtils;
  exports nl.clariah.recipe;
  provides nl.clariah.recipe.Recipe
      with nl.clariah.recipe.Redirect;
}
