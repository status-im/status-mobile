(ns status-im.ui.components.contact.contact
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.checkbox.view :as checkbox]
            [status-im.ui.components.contact.styles :as styles]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.list.views :as list]
            [status-im.utils.gfycat.core :as gfycat]))

(defn- contact-inner-view
  ([{:keys [info style props] {:keys [whisper-identity name dapp?] :as contact} :contact}]
   [react/view (merge styles/contact-inner-container style)
    [react/view
     [chat-icon/contact-icon-contacts-tab contact]]
    [react/view styles/info-container
     [react/text (merge {:style           styles/name-text
                          :number-of-lines 1}
                        (when dapp? {:accessibility-label :dapp-name})
                        props)
      (if (pos? (count (:name contact)))
        (i18n/get-contact-translated whisper-identity :name name)
        ;;TODO is this correct behaviour?
        (gfycat/generate-gfy whisper-identity))]
     (when info
       [react/text {:style styles/info-text}
        info])]]))

(defn contact-view [{:keys [style contact extended? on-press extend-options extend-title info show-forward?
                            accessibility-label inner-props]
                     :or   {accessibility-label :contact-item}}]
  [react/touchable-highlight (merge {:accessibility-label accessibility-label}
                                    (when-not extended?
                                      {:on-press (when on-press #(on-press contact))}))
    [react/view styles/contact-container
     [contact-inner-view {:contact contact :info info :style style :props inner-props}]
     (when show-forward?
       [react/view styles/forward-btn
        [vector-icons/icon :icons/forward]])
     (when (and extended? (not (empty? extend-options)))
       [react/view styles/more-btn-container
        [react/touchable-highlight {:on-press            #(list-selection/show {:options extend-options
                                                                                :title   extend-title})
                                    :accessibility-label :menu-option}
         [react/view styles/more-btn
          [vector-icons/icon :icons/options {:accessibility-label :options}]]]])]])

(views/defview toogle-contact-view [{:keys [whisper-identity] :as contact} selected-key on-toggle-handler]
  (views/letsubs [checked [selected-key whisper-identity]]
    [react/view {:accessibility-label :contact-item}
     [list/list-item-with-checkbox
      {:checked?        checked
       :on-value-change #(on-toggle-handler checked whisper-identity)
       :plain-checkbox? true}
      [react/view styles/contact-container
       [contact-inner-view {:contact contact}]]]]))
