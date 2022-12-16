(ns status-im2.contexts.chat.messages.pin.list.view
  (:require [utils.re-frame :as rf]
            [react-native.core :as rn]
            [quo2.core :as quo]
            [i18n.i18n :as i18n]
            [quo2.foundations.colors :as colors]

            ;; TODO move to status-im2
            [status-im.ui2.screens.chat.messages.message :as old-message]))

(def list-key-fn #(or (:message-id %) (:value %)))

(defn pinned-messages-list [chat-id]
  (let [pinned-messages (vec (vals (rf/sub [:chats/pinned chat-id])))
        current-chat    (rf/sub [:chats/current-chat])
        community       (rf/sub [:communities/community (:community-id current-chat)])]
    [rn/view {:accessibility-label :pinned-messages-list}
     ;; TODO (flexsurfer) this should be a component in quo2 https://github.com/status-im/status-mobile/issues/14529
     [:<>
      [quo/text {:size   :heading-1
                 :weight :semi-bold
                 :style  {:margin-horizontal 20}}
       (i18n/label :t/pinned-messages)]
      (when community
        [rn/view {:style {:flex-direction    :row
                          :background-color  (colors/theme-colors colors/neutral-10 colors/neutral-80)
                          :border-radius     20
                          :align-items       :center
                          :align-self        :flex-start
                          :margin-horizontal 20
                          :padding           4
                          :margin-top        8}}
         [rn/text {:style {:margin-left 6 :margin-right 4}} (:name community)]
         [quo/icon
          :i/chevron-right
          {:color  (colors/theme-colors colors/neutral-50 colors/neutral-40)
           :size  12}]
         [rn/text {:style {:margin-left  4
                           :margin-right 8}}
          (str "# " (:chat-name current-chat))]])]
     (if (> (count pinned-messages) 0)
       [rn/flat-list
        {:data      pinned-messages
         :render-fn old-message/message-render-fn
         :key-fn    list-key-fn
         :separator quo/separator}]
       [rn/view {:style {:justify-content :center
                         :align-items     :center
                         :margin-top      20}}
        [rn/view {:style {:width           120
                          :height          120
                          :justify-content :center
                          :align-items     :center
                          :border-width    1}} [quo/icon :i/placeholder]]
        [quo/text {:weight :semi-bold
                   :style  {:margin-top 20}}
         (i18n/label :t/no-pinned-messages)]
        [quo/text {:size :paragraph-2}
         (i18n/label (if community :t/no-pinned-messages-community-desc :t/no-pinned-messages-desc))]])]))
