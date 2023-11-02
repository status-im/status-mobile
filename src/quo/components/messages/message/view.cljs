(ns quo.components.messages.message.view
  (:require [quo.components.avatars.user-avatar.view :as user-avatar]
            [quo.components.icon :as icon]
            [quo.components.info.info-message :as info-message]
            [quo.components.markdown.text :as text]
            [quo.components.messages.author.view :as author]
            [quo.components.messages.message.style :as style]
            [quo.components.selectors.react.view :as react]
            [quo.foundations.colors :as colors]
            [quo.theme :as theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]))

(defn- pin-indicator
  [{:keys [theme pinned-by customization-color]}]
  [rn/view {:style style/pin-indicator-container}
   [icon/icon :i/pin
    {:color (colors/resolve-color customization-color theme)
     :size  16}]
   [text/text
    {:size   :label
     :weight :medium
     :style  {:margin-left 2 :color (colors/resolve-color customization-color theme)}}
    pinned-by]]
)

(defn- internal-view
  []
  (let [state (reagent/atom :default)]
    (fn [{:keys [theme header? reacted? delivered? avatar-props author-props on-press context
                 customization-color]}]
      (println context)
      (let [{:keys [content-type pinned-by reactions]} context]
        [rn/pressable
         {:style (style/container theme header? reacted? @state (seq pinned-by) customization-color)
          :on-press-in #(reset! state :pressed)
          :on-press-out #(reset! state :default)
          :on-press on-press
         }
         (when (seq pinned-by)
           [pin-indicator
            {:theme               theme
             :header?             header?
             :pinned-by           pinned-by
             :customization-color customization-color}])
         [rn/view {:style {:flex-direction :row :margin-left (when-not header? 40)}}
          (when header?
            [rn/view {:style style/avatar-container}
             [user-avatar/user-avatar
              (merge avatar-props {:size :small})]
            ])
          [rn/view
           (when header? [author/view (merge author-props {:size 13})])
           [rn/view {:style style/message-content}
            (condp = content-type
              :text
              [text/text "This is a simple message"]
              :image
              [text/text "image"]
              :text-and-image
              [text/text "image with text"]
              :audio
              [text/text "audio"]
              :gif
              [text/text "gif"]
              :sticker
              [text/text "sticker"]
              :text-and-code
              [text/text "code with text"]
              :code
              [text/text "code"]
              :text-and-link-preview
              [text/text "link preview with text"]
              :text-and-internal-link
              [text/text "internal link with text"]
              :internal-link
              [text/text "internal link"]
              :text-and-forward-message
              [text/text "forward message with text"]
              :forward-message
              [text/text "forward message"]

              [text/text "not implemented"])]
           (when (seq reactions)
             [react/view
              {:reactions       reactions
               :use-case        :pinned
               :add-reaction?   true
               :container-style {:margin-top 8}}])
           (when delivered?
             [info-message/info-message
              {:type  :default
               :size  :tiny
               :icon  :i/sent
               :style {:margin-top 2}
              } (i18n/label :t/sent)])
          ]]
        ])
    ))
)

(def view (theme/with-theme internal-view))
