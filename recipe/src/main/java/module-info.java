module nl.clariah.recipe {
  requires Saxon.HE;
  requires SaxonUtils;
  exports nl.clariah.recipe;
  provides nl.clariah.recipe.Cat
      with nl.clariah.recipe.Cat;
  provides nl.clariah.recipe.Redirect
      with nl.clariah.recipe.Redirect;
}
