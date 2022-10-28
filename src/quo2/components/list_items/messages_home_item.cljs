(ns quo2.components.list-items.messages-home-item
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.utils.handlers :refer [<sub >evt]]
            [status-im.utils.datetime :as time]
            [quo2.foundations.typography :as typography]
            [quo2.components.notifications.info-count :refer [info-count]]
            [quo2.components.icon :as icons]
            [quo2.foundations.colors :as colors]
            [quo2.components.avatars.user-avatar :as user-avatar]
            [quo.react-native :as rn]
            [status-im.ui.screens.chat.sheets :as sheets]
            [quo.platform :as platform]))


(def max-subheader-length 100)

(defn truncate-literal [literal]
  (when literal
    (let [size (min max-subheader-length (.-length literal))]
      {:components (.substring literal 0 size)
       :length     size})))

(defn add-parsed-to-subheader [acc {:keys [type destination literal children]}]
  (let [result (case type
                 "paragraph"
                 (reduce
                   (fn [{:keys [_ length] :as acc-paragraph} parsed-child]
                     (if (>= length max-subheader-length)
                       (reduced acc-paragraph)
                       (add-parsed-to-subheader acc-paragraph parsed-child)))
                   {:components [rn/text]
                    :length     0}
                   children)

                 "mention"
                 {:components [rn/text @(re-frame/subscribe [:contacts/contact-name-by-identity literal])]
                  :length     4} ;; we can't predict name length so take the smallest possible

                 "status-tag"
                 (truncate-literal (str "#" literal))

                 "link"
                 (truncate-literal destination)

                 (truncate-literal literal))]
    {:components (conj (:components acc) (:components result))
     :length     (+ (:length acc) (:length result))}))

(defn render-subheader
  "Render the preview of a last message to a maximum of max-subheader-length characters"
  [parsed-text]
  (let [result
        (reduce
          (fn [{:keys [_ length] :as acc-text} new-text-chunk]
            (if (>= length max-subheader-length)
              (reduced acc-text)
              (add-parsed-to-subheader acc-text new-text-chunk)))
          {:components [rn/text {:style               (merge typography/paragraph-2 typography/font-regular
                                                             {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)
                                                              :width "90%"})
                                 :number-of-lines     1
                                 :ellipsize-mode      :tail
                                 :accessibility-label :chat-message-text}]
           :length     0}
          parsed-text)]
    (:components result)))


(defn messages-home-item [item]
  (let [{:keys [chat-id color group-chat last-message timestamp name]} item
        display-name (if-not group-chat (first (<sub [:contacts/contact-two-names-by-identity chat-id])) name)
        contact      (if-not group-chat (<sub [:contacts/contact-by-address chat-id]) nil)
        photo-path   (if-not (empty? (:images contact)) (<sub [:chats/photo-path chat-id]) nil)]
    ; TODO: community chats are not yet implemented. Some backend implementations are needed first. Also designs are not yet available.
    [rn/touchable-opacity (merge {:style         {:margin-top         8
                                                  :margin-horizontal  8
                                                  :padding-vertical   8
                                                  :padding-horizontal 12
                                                  :border-radius      12
                                                  :flex-direction     :row
                                                  :align-items        :center}
                                  :on-press      (fn []
                                                   (re-frame/dispatch [:dismiss-keyboard])
                                                   (if platform/android?
                                                     (re-frame/dispatch [:chat.ui/navigate-to-chat-nav2 chat-id])
                                                     (re-frame/dispatch [:chat.ui/navigate-to-chat chat-id]))
                                                   (re-frame/dispatch [:search/home-filter-changed nil])
                                                   (re-frame/dispatch [:accept-all-activity-center-notifications-from-chat chat-id]))
                                  :on-long-press #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                                      {:content (fn [] [sheets/actions item])}])})
     (if group-chat
       [rn/view {:style {:width            32
                         :height           32
                         :background-color color
                         :justify-content  :center
                         :align-items      :center
                         :border-radius    16}}
        [icons/icon :main-icons2/group {:size 16 :color colors/white-opa-70}]]
       [user-avatar/user-avatar {:full-name         display-name
                                 :profile-picture   photo-path
                                 :status-indicator? true
                                 :online?           true
                                 :size              :small
                                 :ring?             false}])

     [rn/view {:style {:margin-left 8}}
      [rn/view {:style {:flex-direction :row}}
       [rn/text {:style (merge typography/paragraph-1 typography/font-semi-bold
                               {:color (colors/theme-colors colors/neutral-100 colors/white)})}
        display-name]
       ; TODO: real name need to be also displayed if set. Currently user model does not have a "real-name" field
       (if (:ens-verified contact)
         [rn/view {:style {:margin-left 5 :margin-top 4}}
          [icons/icon :main-icons2/verified {:size 12 :color (colors/theme-colors colors/success-50 colors/success-60)}]]
         (when (:added? contact)
           [rn/view {:style {:margin-left 5 :margin-top 4}}
            [icons/icon :main-icons2/contact {:size 12 :color (colors/theme-colors colors/primary-50 colors/primary-60)}]]))
       [rn/text {:style (merge typography/font-regular typography/label
                               {:color       (colors/theme-colors colors/neutral-50 colors/neutral-40)
                                :margin-top  3
                                :margin-left 8})}
        (time/to-short-str (or timestamp "9999999999999"))]] ; placeholder for community chats to avoid crashing until implemented
      (if (string/blank? (get-in last-message [:content :parsed-text]))
        [rn/text {:style (merge typography/paragraph-2 typography/font-regular
                                {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)})}
         (get-in last-message [:content :text])]
        [render-subheader (get-in last-message [:content :parsed-text])])]
     (if (> (:unviewed-mentions-count item) 0)
       [info-count (:unviewed-mentions-count item) {:top 16}]
       (when (> (:unviewed-messages-count item) 0)
         [rn/view {:style {:width            8
                           :height           8
                           :border-radius    4
                           :position         :absolute
                           :right            26
                           :top              16
                           :background-color (colors/theme-colors colors/neutral-40 colors/neutral-60)}}]))]
    ))
