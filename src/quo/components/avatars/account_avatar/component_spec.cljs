(ns quo.components.avatars.account-avatar.component-spec
  (:require
    [quo.components.avatars.account-avatar.style :as style]
    [quo.components.avatars.account-avatar.view :as account-avatar]
    [quo.foundations.colors :as colors]
    [test-helpers.component :as h]))

(h/describe "Account Avatar"
  (h/test "default render"
    (h/render [account-avatar/view])
    (h/is-truthy (h/query-by-label-text :account-avatar))
    (h/is-truthy (h/query-by-label-text :account-emoji)))

  (h/test "with emoji"
    (let [emoji "💸"]
      (h/render [account-avatar/view {:emoji emoji :size 80}])
      (h/is-truthy (h/query-by-label-text :account-avatar))
      (h/is-truthy (h/query-by-label-text :account-emoji))
      (h/is-truthy (h/query-by-text emoji))))

  (h/test "size 80 with emoji, with type - default"
    (let [opts {:emoji               "🏝️"
                :size                80
                :type                :default
                :customization-color :blue}]
      (h/render [account-avatar/view opts])
      (h/is-truthy (h/query-by-label-text :account-avatar))
      (h/has-style (h/get-by-label-text :account-avatar)
                   {:height          (:size opts)
                    :width           (:size opts)
                    :borderRadius    (style/get-border-radius (:size opts))
                    :backgroundColor (colors/resolve-color (:customization-color opts) :light)})
      (h/is-truthy (h/query-by-label-text :account-emoji))
      (h/has-style (h/query-by-label-text :account-emoji)
                   {:fontSize (style/get-emoji-size (:size opts))})
      (h/is-truthy (h/query-by-text (:emoji opts)))))

  (h/test "size 48 with emoji, with type - watch only"
    (let [opts {:emoji               "💵"
                :size                48
                :type                :watch-only
                :customization-color :purple}]
      (h/render [account-avatar/view opts])
      (h/is-truthy (h/query-by-label-text :account-avatar))
      (h/has-style
       (h/get-by-label-text :account-avatar)
       {:height          (:size opts)
        :width           (:size opts)
        :borderRadius    (style/get-border-radius (:size opts))
        :borderWidth     1
        :backgroundColor (colors/resolve-color (:customization-color opts) :light 10)})
      (h/is-truthy (h/query-by-label-text :account-emoji))
      (h/has-style (h/query-by-label-text :account-emoji)
                   {:fontSize (style/get-emoji-size (:size opts))})
      (h/is-truthy (h/query-by-text (:emoji opts)))))

  (h/test "size 28 with emoji, with type - default"
    (let [opts {:emoji               "🏝️"
                :size                28
                :type                :default
                :customization-color :turquoise}]
      (h/render [account-avatar/view opts])
      (h/is-truthy (h/query-by-label-text :account-avatar))
      (h/has-style (h/get-by-label-text :account-avatar)
                   {:height          (:size opts)
                    :width           (:size opts)
                    :borderRadius    (style/get-border-radius (:size opts))
                    :backgroundColor (colors/resolve-color (:customization-color opts) :light)})
      (h/is-truthy (h/query-by-label-text :account-emoji))
      (h/has-style (h/query-by-label-text :account-emoji)
                   {:fontSize (style/get-emoji-size (:size opts))})
      (h/is-truthy (h/query-by-text (:emoji opts)))))

  (h/test "size 16 with emoji, with type - watch only"
    (let [opts {:emoji               "🎉"
                :size                16
                :type                :watch-only
                :customization-color :copper}]
      (h/render [account-avatar/view opts])
      (h/is-truthy (h/query-by-label-text :account-avatar))
      (h/has-style
       (h/get-by-label-text :account-avatar)
       {:height          (:size opts)
        :width           (:size opts)
        :borderRadius    (style/get-border-radius (:size opts))
        :borderWidth     0.8
        :backgroundColor (colors/resolve-color (:customization-color opts) :light 10)})
      (h/is-truthy (h/query-by-label-text :account-emoji))
      (h/has-style (h/query-by-label-text :account-emoji)
                   {:fontSize (style/get-emoji-size (:size opts))})
      (h/is-truthy (h/query-by-text (:emoji opts))))))
