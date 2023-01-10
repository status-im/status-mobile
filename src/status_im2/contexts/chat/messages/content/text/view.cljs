(ns status-im2.contexts.chat.messages.content.text.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [status-im2.contexts.chat.messages.content.text.style :as style]
    [status-im2.contexts.chat.messages.link-preview.view :as link-preview]
    [utils.re-frame :as rf]))


(defn render-inline
  [units {:keys [type literal destination]}]
  (case (keyword type)
    :code
    (conj units [rn/view {:style (merge style/block (style/code))} [quo/text {:weight :code} literal]])

    :emph
    (conj units [quo/text {:style {:font-style :italic}} literal])

    :strong
    (conj units [quo/text {:weight :bold} literal])

    :strong-emph
    (conj units
          [quo/text
           {:weight :bold
            :style  {:font-style :italic}} literal])

    :del
    (conj units [quo/text {:style {:text-decoration-line :line-through}} literal])

    :link
    (conj units
          [quo/text
           {:style    {:color                (colors/theme-colors colors/primary-50 colors/primary-60)
                       :text-decoration-line :underline}
            :on-press #(rf/dispatch [:browser.ui/message-link-pressed destination])}
           destination])

    :mention
    (conj
      units
      [rn/touchable-opacity
       {:active-opacity 1
        :on-press       #(rf/dispatch [:chat.ui/show-profile literal])
        :style          (merge style/block {:background-color colors/primary-50-opa-10})}
       [quo/text
        {:weight :medium
         :style  {:color (colors/theme-colors colors/primary-50 colors/primary-60)}}
        (rf/sub [:contacts/contact-name-by-identity literal])]])

    (conj units literal)))


(defn render-block
  [blocks {:keys [type ^js literal children]}]
  (case (keyword type)
    :paragraph
    (conj blocks
          (reduce
            render-inline
            [quo/text]
            children))

    :blockquote
    (conj blocks
          [rn/view {:style style/quote}
           [quo/text literal]])

    :codeblock
    (conj blocks
          [rn/view {:style (merge style/block (style/code))}
           [quo/text (subs literal 0 (dec (count literal)))]])
    blocks))

(defn render-parsed-text
  [{:keys [content]}]
  (reduce render-block
          [:<>]
          (:parsed-text content)))

(defn text-content
  [message-data context]
  [rn/view
   [render-parsed-text message-data]
   [link-preview/link-preview message-data context]])

