(ns quo.components.text-combinations.page-top.component-spec
  (:require [quo.components.text-combinations.page-top.view :as page-top]
            [test-helpers.component :as h]))

(defonce mock-picture {:uri (js/require "../resources/images/mock2/user_picture_male4.png")})

(def context-tag-data
  {:type                :community
   :state               :default
   :customization-color :army
   :community-logo      mock-picture
   :community-name      "Coinbase"
   :emoji               "😝"})

(h/describe "Page Top"
  (h/test "Default render"
    (h/render [page-top/view {:title "Title"}])
    (h/is-truthy (h/get-by-text "Title")))

  (h/test "Avatar and Title"
    (h/render [page-top/view
               {:title  "Title"
                :avatar {:emoji               "🥨"
                         :customization-color :army}}])

    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-label-text :channel-avatar)))

  (h/describe "Description"
    (h/test "Text"
      (h/render [page-top/view
                 {:title            "Title"
                  :description      :text
                  :description-text "This is a textual description"}])

      (h/is-truthy (h/get-by-text "Title"))
      (h/is-truthy (h/get-by-text "This is a textual description")))

    (h/test "Context tag"
      (h/render-with-theme-provider [page-top/view
                                     {:title       "Title"
                                      :description :context-tag
                                      :context-tag context-tag-data}])

      (h/is-truthy (h/get-by-text "Title"))
      (h/is-truthy (h/get-by-label-text :context-tag)))

    (h/test "Summary"
      (h/render-with-theme-provider [page-top/view
                                     {:title       "Title"
                                      :description :summary
                                      :summary     {:row-1 {:text-1        "Send"
                                                            :text-2        "from"
                                                            :context-tag-1 context-tag-data
                                                            :context-tag-2 context-tag-data}
                                                    :row-2 {:text-1        "to"
                                                            :text-2        "via"
                                                            :context-tag-1 context-tag-data
                                                            :context-tag-2 context-tag-data}}}])

      (h/is-truthy (h/get-by-text "Title"))
      (h/is-truthy (h/get-by-text "Send"))
      (h/is-truthy (h/get-by-text "from"))
      (h/is-truthy (h/get-by-text "to"))
      (h/is-truthy (h/get-by-text "via"))
      (h/is-equal (count (js->clj (h/get-all-by-label-text :context-tag))) 4))

    (h/test "Collection"
      (h/render [page-top/view
                 {:title            "Title"
                  :description      :collection
                  :collection-text  "Collectible Collection"
                  :collection-image mock-picture}])

      (h/is-truthy (h/get-by-text "Title"))
      (h/is-truthy (h/get-by-text "Collectible Collection"))
      (h/is-truthy (h/get-by-label-text :collection-avatar)))

    (h/test "Community"
      (h/render [page-top/view
                 {:title           "Title"
                  :description     :community
                  :community-text  "Doodles"
                  :community-image mock-picture}])

      (h/is-truthy (h/get-by-text "Title"))
      (h/is-truthy (h/get-by-text "Doodles"))
      (h/is-truthy (h/get-by-label-text :community-logo))))

  (h/test "Emoji dash"
    (let [emoji-dash ["❤️" "✏️" "💬" "😋" "📱" "🚓" "💹" "😝" "👊" "👤" "😚" "🚉" "👻" "\uD83D\uDC6F"]]
      (h/render [page-top/view
                 {:title      "Title"
                  :emoji-dash emoji-dash}])

      (doseq [emoji emoji-dash]
        (h/is-truthy (h/get-by-text emoji)))
      (h/is-truthy (h/get-by-text "Title"))))

  (h/test "Search input"
    (h/render [page-top/view
               {:title "Title"
                :input :search}])

    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-label-text :search-input)))

  (h/test "Address input"
    (h/render [page-top/view
               {:title "Title"
                :input :address}])

    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-label-text :address-text-input)))

  (h/test "Recovery phrase"
    (h/render [page-top/view
               {:title "Title"
                :input :recovery-phrase}])

    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-label-text :recovery-phrase-input))))
