(ns quo2.components.list-items.received-contact-request
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [status-im.utils.handlers :refer [<sub >evt]]
            [quo2.components.avatars.user-avatar :as user-avatar]
            [quo2.foundations.typography :as typography]
            [clojure.string :as str]
            [status-im.utils.utils :as utils.utils]
            [status-im.utils.datetime :as time]
            [status-im.i18n.i18n :as i18n]))

(defn get-display-name [chat-id no-ens-name no-nickname]
  (let [name (first (<sub [:contacts/contact-two-names-by-identity chat-id]))]
    (if (and no-ens-name no-nickname)
      (str (first (str/split name " ")) " " (second (str/split name " ")))
      name)))

(defn list-item [{:keys [chat-id image contact message timestamp]}]
  (let [no-ens-name  (str/blank? (:ens-name (:content message)))
        no-nickname  (if (= (:nickname (:names contact)) nil) true false)
        display-name (get-display-name chat-id no-ens-name no-nickname)]
    [rn/view {:style {:flex-direction :row
                      :padding-top    8
                      :margin-top     4
                      :padding-bottom 12}}
     [user-avatar/user-avatar {:full-name         display-name
                               :status-indicator? true
                               :online?           true
                               :size              :small
                               :profile-picture   image
                               :ring?             false}]
     [rn/view {:style {:margin-horizontal 8}}
      [rn/view {:style {:flex-direction :row}}
       [rn/text {:style (merge typography/font-semi-bold typography/paragraph-1
                               {:color        (colors/theme-colors "#000000" "#ffffff")
                                :margin-right 8})} display-name]
       (when no-ens-name [rn/text {:style (merge typography/font-regular typography/label
                                                 {:color      (colors/theme-colors colors/neutral-50 colors/neutral-40)
                                                  :margin-top 4})}
                          (str (utils.utils/get-shortened-address chat-id) " · ")])
       [rn/text {:style (merge typography/font-regular typography/label
                               {:color      (colors/theme-colors colors/neutral-50 colors/neutral-40)
                                :margin-top 4})}
        (time/to-short-str timestamp)]]
      [rn/view {:style {:border-radius      12
                        :margin-top         10
                        :padding-horizontal 12
                        :padding-vertical   8
                        :border-width       1
                        :border-color       (colors/theme-colors colors/neutral-20 colors/neutral-70)}}
       [rn/text {:style (merge typography/font-regular
                               typography/paragraph-1
                               {:color (colors/theme-colors "000000" "#ffffff")})}
        (:text (:content message))]]
      [rn/view {:style {:margin-top     12
                        :flex-direction :row}}
       [rn/touchable-opacity {:accessibility-label :decline-cr
                              :on-press            #(>evt [:contact-requests.ui/decline-request (:message-id message)])
                              :active-opacity      1
                              :style               {:background-color   (colors/theme-colors colors/danger-50 colors/danger-60)
                                                    :justify-content    :center
                                                    :align-items        :center
                                                    :align-self         :flex-start
                                                    :border-radius      8
                                                    :padding-vertical   4
                                                    :padding-horizontal 8}}
        [rn/text {:style (merge typography/font-medium typography/paragraph-2 {:color "#ffffff"})} (i18n/label :t/decline)]]
       [rn/touchable-opacity {:accessibility-label :accept-cr
                              :on-press            #(>evt [:contact-requests.ui/accept-request (:message-id message)])
                              :active-opacity      1
                              :style               {:background-color   (colors/theme-colors colors/success-50 colors/success-60)
                                                    :justify-content    :center
                                                    :align-items        :center
                                                    :align-self         :flex-start
                                                    :border-radius      8
                                                    :padding-vertical   4
                                                    :padding-horizontal 8
                                                    :margin-left        8}}
        [rn/text {:style (merge typography/font-medium typography/paragraph-2 {:color "#ffffff"})} (i18n/label :t/accept)]]]]]))
