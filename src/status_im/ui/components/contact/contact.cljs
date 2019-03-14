(ns status-im.ui.components.contact.contact
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.i18n :as i18n]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.contact.styles :as styles]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.list.views :as list]
            [status-im.utils.gfycat.core :as gfycat]
            [clojure.string :as string]))

(defn desktop-extended-options [options]
  [react/view {}
   (doall (for [{:keys [label action]} options]
            ^{:key label}
            [react/touchable-highlight
             {:on-press action}
             [react/view {}
              [react/text label]]]))])

(defn contact-view
  [{:keys [style contact extended? on-press extend-options extend-title
           info show-forward? accessibility-label inner-props]
    {:keys [public-key name]} :contact
    :or   {accessibility-label :contact-item}}]
  [react/touchable-highlight (merge {:accessibility-label accessibility-label}
                                    (when-not extended?
                                      {:on-press (when on-press #(on-press contact))}))
   [react/view styles/contact-container-to-refactor
    [chat-icon/contact-icon-contacts-tab contact]
    [react/view styles/info-container-to-refactor
     [react/text (merge {:style           styles/name-text
                         :number-of-lines 1}
                        inner-props)
      (if (string/blank? name)
        (gfycat/generate-gfy public-key)
        (or name (i18n/label :t/chat-name)))]
     (when info
       [react/text {:style styles/info-text}
        info])]
    (when show-forward?
      [react/view styles/forward-btn
       [vector-icons/icon :main-icons/next]])
    (when (and extended? (not (empty? extend-options)))
      (if platform/desktop?
        (desktop-extended-options extend-options)
        [react/view styles/more-btn-container
         [react/touchable-highlight {:on-press            #(list-selection/show {:options extend-options
                                                                                 :title   extend-title})
                                     :accessibility-label :menu-option}
          [react/view styles/more-btn
           [vector-icons/icon :main-icons/more {:accessibility-label :options}]]]]))]])

(views/defview toggle-contact-view
  [{:keys [public-key name] :as contact} selected-key on-toggle-handler disabled?]
  (views/letsubs [checked [selected-key public-key]]
    [react/view {:accessibility-label :contact-item}
     [list/list-item-with-checkbox
      {:checked?        checked
       :disabled?       disabled?
       :on-value-change #(on-toggle-handler checked public-key)}
      [list/item
       [chat-icon/contact-icon-contacts-tab contact]
       [react/view styles/info-container
        [react/text {:style           styles/name-text
                     :number-of-lines 1}
         (if (string/blank? name)
           (gfycat/generate-gfy public-key)
           (or name (i18n/label :t/chat-name)))]]]]]))
