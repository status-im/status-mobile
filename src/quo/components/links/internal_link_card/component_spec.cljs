(ns quo.components.links.internal-link-card.component-spec
  (:require
    [quo.components.links.internal-link-card.view :as view]
    [test-helpers.component :as h]))

(def user-props
  {:title               "Some title"
   :subtitle            "Some description"
   :loading?            false
   :icon                "data:image/png,logo-x"
   :type                :user
   :customization-color "#ff0000"
   :emojis              [:i/active-members :i/arrow-left]})

(h/describe "Internal link card - User"
  (h/test "renders with most common props"
    (h/render [view/view user-props])
    (h/is-truthy (h/query-by-text (:title user-props)))
    (h/is-truthy (h/query-by-text (:subtitle user-props))))

  (h/test "does not render logo if prop is not present"
    (h/render [view/view (dissoc user-props :icon)])
    (h/is-null (h/query-by-label-text :logo))))

(def community-props
  {:title                "Some title"
   :description          "Some description"
   :icon                 "data:image/png,logo-x"
   :banner               "data:image/png,whatever"
   :members-count        "20"
   :loading?             false
   :active-members-count "15"
   :type                 :community})

(h/describe "Internal link card - Community"
  (h/test "renders with most common props"
    (h/render [view/view community-props])
    (h/is-truthy (h/query-by-text (:title community-props)))
    (h/is-truthy (h/query-by-text (:description community-props)))
    (h/is-truthy (h/query-by-text (:members-count community-props)))
    (h/is-truthy (h/query-by-text (:active-members-count community-props)))
    (h/is-truthy (h/query-by-label-text :logo))
    (h/is-truthy (h/query-by-label-text :thumbnail)))

  (h/test "does not render thumbnail if prop is not present"
    (h/render [view/view (dissoc community-props :banner)])
    (h/is-null (h/query-by-label-text :thumbnail)))

  (h/test "does not render logo if prop is not present"
    (h/render [view/view (dissoc community-props :icon)])
    (h/is-null (h/query-by-label-text :logo))))

(def channel-props
  {:title        "Doodles"
   :description  "Coloring the world with joy • ᴗ •"
   :icon         "data:image/png,logo-x"
   :banner       "data:image/png,whatever"
   :loading      false
   :channel-name "#general"
   :type         :channel})

(h/describe "Internal link card - Channel"
  (h/test "renders with most common props"
    (h/render [view/view channel-props])
    (h/is-truthy (h/query-by-text (:title channel-props)))
    (h/is-truthy (h/query-by-text (:description channel-props)))
    (h/is-truthy (h/query-by-text (:channel-name channel-props)))
    (h/is-truthy (h/query-by-label-text :logo))
    (h/is-truthy (h/query-by-label-text :banner)))

  (h/test "does not render banner if prop is not present"
    (h/render [view/view (dissoc channel-props :banner)])
    (h/is-null (h/query-by-label-text :banner)))

  (h/test "does not render logo if prop is not present"
    (h/render [view/view (dissoc channel-props :icon)])
    (h/is-null (h/query-by-label-text :logo))))
