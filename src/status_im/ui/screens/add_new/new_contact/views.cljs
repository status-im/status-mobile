(ns status-im.ui.screens.add-new.new-contact.views
  (:require [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.invite.events :as invite.events]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.utils.db :as utils.db]
            [status-im.utils.utils :as utils]
            [utils.debounce :as debounce]
            [utils.re-frame :as rf]
            [quo.core :as quo]
            [quo.react-native :as rn]
            [quo2.core :as quo2]
            [quo2.foundations.colors :as colors]))

;; TODO(@esep): move styles to styles.cljs (https://github.com/status-im/status-mobile/blob/develop/doc/new-guidelines.md#component-styles)
;; TODO(@esep): i18n (https://github.com/status-im/status-mobile/blob/develop/doc/new-guidelines.md#component-styles)

(defn get-validation-label [value]
  (case value
    :invalid
    (i18n/label :t/profile-not-found)
    :yourself
    (i18n/label :t/can-not-add-yourself)))

(def x (atom {}))
(defn new-contact []
  (let [new-identity (rf/sub [:contacts/new-identity])
        {:keys [state ens-name public-key error]} new-identity
        blocked? (and
                  (utils.db/valid-public-key? (or public-key ""))
                  (rf/sub [:contacts/contact-blocked? public-key]))]
    (println "state:" state)
    ;; (swap! x assoc :contacts contacts)
    ;; (swap! x assoc :account account)
    [rn/view
     [rn/view {:height 20}]
     [rn/view {:flex-direction  :row
               :align-items     :flex-start
               :justify-content :flex-start
               :padding-left    20
               :height          44}
      [rn/view {:justify-content  :center
                :align-items      :center
                :background-color colors/neutral-10
                :width            32
                :height           32
                :border-radius    10}
       [rn/touchable-opacity {:on-press #(rf/dispatch [:navigate-back])}
        [icons/icon :main-icons2/close]]]]
     [rn/image {:source (js/require "../resources/images/ui/add-contact.png")
                ;; :style styles/sync-devices-header-image
                }]
     [rn/view {:height         400
               :flex-direction :column
               :padding        16
               :border-width   1
               :border-color   colors/primary-50
               }
      [quo2/text {:size   :heading-1
                  :weight :semi-bold
                  :style  {:color colors/neutral-100}} "Add a contact"]
      [quo2/text {:size   :paragraph-2
                  :weight :regular
                  :style  {:color colors/neutral-100}} "Find your friends with their ENS or Chat key"]
      [quo2/text {:size   :paragraph-2
                  :weight :regular
                  :style  {:color colors/neutral-100}} "ENS or Chat key"]
      [rn/view {;; :flex           1
                ;; :flex-direction  :row
                ;; :align-items     :flex-start
                ;; :justify-content :flex-start
                :border-width 1
                :border-color colors/primary-50
                ;; :padding 16
                ;; :width          280
                }
       [quo/text-input
        {:on-change-text
         #(do
            (rf/dispatch [:set-in [:contacts/new-identity :state] :searching])
            (debounce/debounce-and-dispatch
             [:new-chat/set-new-identity %] 600))
         :on-submit-editing   #()
         ;; #(when (= state :valid)
         ;;    (debounce/dispatch-and-chill
         ;;     [:contact.ui/contact-code-submitted true @entered-nickname] 3000))
         :placeholder         "0x123abc"
         :show-cancel         false
         :accessibility-label :enter-contact-code-input
         :auto-capitalize     :none
         :height              40
         :width               283
         :background-color    colors/white
         :border-width        1
         :border-color        colors/neutral-20
         :border-radius       12
         ;; :return-key-type     :go
         }]
       [rn/view {:justify-content  :center
                 :align-items      :center
                 :width            32
                 :height           32
                 :background-color colors/white
                 :border-width     1
                 :border-color     colors/neutral-20
                 :border-radius    12}
        [rn/touchable-opacity {:on-press #()}
         [icons/icon :main-icons2/scan2]]]]
      [quo/text {:style {:margin-horizontal 16}
                 :size  :small
                 :align :center
                 :color :secondary}
       (cond (= state :error)
             (get-validation-label error)
             (= state :valid)
             (str (when ens-name (str ens-name " • "))
                  (utils/get-shortened-address public-key))
             :else "")]]
     [rn/view {:padding 16}
      [quo2/button {:type                :primary
                    :size                40
                    :width               335
                    :accessibility-label :new-contact-button
                    :before              :i/profile
                    :disabled            (if (= state :valid) false true)
                    :on-press
                    (if (= state :valid)
                      #(rf/dispatch [:chat.ui/show-profile public-key])
                      #())}
       "View profile"]]]))

(defn- icon-wrapper [color icon]
  [react/view
   {:style {:width            50
            :height           32
            :border-radius    25
            :align-items      :center
            :justify-content  :center
            :background-color color}}
   icon])

(defn- input-icon
  [state new-contact? entered-nickname blocked?]
  (let [icon (if new-contact? :main-icons/add :main-icons/arrow-right)]
    (cond
      (= state :searching)
      [icon-wrapper colors/neutral-40
       [react/activity-indicator {:color colors/neutral-5}]]

      (and (= state :valid) (not blocked?))
      [react/touchable-highlight
       {:on-press #(debounce/dispatch-and-chill [:contact.ui/contact-code-submitted new-contact? entered-nickname] 3000)}
       [icon-wrapper colors/primary-50
        [icons/icon icon {:color colors/neutral-5}]]]

      :else
      [icon-wrapper colors/neutral-40
       [icons/icon icon {:color colors/neutral-5}]])))

(defn- nickname-input [entered-nickname]
  [quo/text-input
   {:on-change-text      #(reset! entered-nickname %)
    :auto-capitalize     :none
    :max-length          32
    :auto-focus          false
    :accessibility-label :nickname-input
    :placeholder         (i18n/label :t/add-nickname)
    :return-key-type     :done
    :auto-correct        false}])

(def y (atom {}))
(defn new-contact-old []
  (let [entered-nickname (reagent/atom "")]
    (fn []
      (let [new-identity (rf/sub [:contacts/new-identity])
            {:keys [state ens-name public-key error]} new-identity
            blocked? (and
                      (utils.db/valid-public-key? (or public-key ""))
                      (rf/sub [:contacts/contact-blocked? public-key]))]
        ;; (swap! y :new-identity new-identity)
        [rn/view {:style {:flex 1}}
         [topbar/topbar
          {:title  (i18n/label :t/new-contact)
           :modal? true
           :right-accessories
           [{:icon                :qr
             :accessibility-label :scan-contact-code-button
             :on-press
             #(rf/dispatch [::qr-scanner/scan-code
                            {:title        (i18n/label :t/new-contact)
                             :handler      :contact/qr-code-scanned
                             :new-contact? true
                             :nickname     @entered-nickname}])}]}]
         [rn/view {:flex-direction :row
                   :padding        16}
          [rn/view {:flex          1
                    :padding-right 16}
           [quo/text-input
            {:on-change-text
             #(do
                (rf/dispatch [:set-in [:contacts/new-identity :state] :searching])
                (debounce/debounce-and-dispatch
                 [:new-chat/set-new-identity %] 600))
             :on-submit-editing
             #(when (= state :valid)
                (debounce/dispatch-and-chill
                 [:contact.ui/contact-code-submitted true @entered-nickname] 3000))
             :placeholder         (i18n/label :t/enter-contact-code)
             :show-cancel         false
             :accessibility-label :enter-contact-code-input
             :auto-capitalize     :none
             :return-key-type     :go}]]
          [rn/view {:justify-content :center
                    :border-width 1
                    :border-color colors/danger-50
                    :align-items     :center}
           [input-icon state true @entered-nickname blocked?]]]
         [rn/view {:min-height 30 :justify-content :flex-end :margin-bottom 16}
          [quo/text {:style {:margin-horizontal 16}
                     :size  :small
                     :align :center
                     :color :secondary}
           (cond (= state :error)
                 (get-validation-label error)
                 (= state :valid)
                 (str (when ens-name (str ens-name " • "))
                      (utils/get-shortened-address public-key))
                 :else "")]]
         [react/text {:style {:margin-horizontal 16 :color colors/neutral-40}}
          (i18n/label :t/nickname-description)]
         [rn/view {:padding 16}
          [nickname-input entered-nickname]
          [react/text {:style {:align-self :flex-end :margin-top 16
                               :color      colors/neutral-40}}
           (str (count @entered-nickname) " / 32")]]]))))

(comment
  ;; open new-chat
  (rf/dispatch [:open-modal :new-chat])
  (rf/dispatch [:open-modal :new-contact])
  ;; close it
  (rf/dispatch [:navigate-back])
  (rf/dispatch [:logout])

  ;; go to new contact (old one), dispatch below, watch, click plus button
  ;; bass
  (rf/dispatch [:new-chat/set-new-identity "0x041adb97f30458f5c5978f5ee82727f4d8f26f94dbfe23a43f59b938f41e3dec070e35dda4fd4315e244d45cb30f45b98ac0a9bcdd366b5102f3ecad6fe193f677"])
  ;; marlin
  (rf/dispatch [:new-chat/set-new-identity "0x04351fb713ee1fd8b15cc70918ccb852176f6f4068af9928051d2f78148cbd850fa6f72550142f2c8195082799d9ef90b226cd4183c67e0ad56c657c211cd83ed6"])
  (rf/dispatch [:chat.ui/show-profile "0x04351fb713ee1fd8b15cc70918ccb852176f6f4068af9928051d2f78148cbd850fa6f72550142f2c8195082799d9ef90b226cd4183c67e0ad56c657c211cd83ed6"])

  )
