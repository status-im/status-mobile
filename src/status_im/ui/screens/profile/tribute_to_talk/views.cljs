(ns status-im.ui.screens.profile.tribute-to-talk.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.colors :as colors]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.common.common :as components.common]
            [status-im.utils.money :as money]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [taoensso.timbre :as log]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.common.styles :as components.common.styles]
            [clojure.string :as string]
            [status-im.utils.config :as config]
            [status-im.utils.utils :as utils]
            [status-im.ui.screens.profile.tribute-to-talk.styles :as styles]
            [status-im.i18n :as i18n]
            [status-im.ui.components.styles :as common.styles]
            [status-im.utils.platform :as platform]))

(defn separator []
  [react/view {:style styles/separator-style}])

(defn steps-numbers [editing?]
  {:intro                1
   :set-snt-amount       (if editing? 1 2)
   :personalized-message (if editing? 2 3)
   :finish               3})

(def step-forward-label
  {:intro                :t/get-started
   :set-snt-amount       :t/continue
   :personalized-message :t/tribute-to-talk-sign-and-set-tribute
   :finish               :t/ok-got-it})

(defn intro []
  [react/view {:style styles/intro-container}
   [components.common/image-contain {:container-style styles/intro-image}
    (:tribute-to-talk resources/ui)]
   [react/view
    [react/i18n-text {:style styles/intro-text
                      :key   :tribute-to-talk}]
    [react/i18n-text {:style (assoc styles/description-label :margin-top 12)
                      :key   :tribute-to-talk-desc}]
    [react/view {:style styles/learn-more-link}
     [react/text {:style styles/learn-more-link-text
                  :on-press #(re-frame/dispatch [:set-in [:my-profile/tribute-to-talk :step] :learn-more])}
      (i18n/label :t/learn-more)]]]])

(defview snt-asset-value []
  (letsubs [snt-amount [:get-in [:my-profile/tribute-to-talk :snt-amount]]
            prices [:prices]
            currency [:wallet/currency]]
    (let [fiat-price (if snt-amount
                       (money/fiat-amount-value snt-amount
                                                :SNT
                                                (-> currency :code keyword)
                                                prices)
                       "0")]
      [react/text {:style styles/snt-asset-value}
       (str "~" fiat-price " " (:code currency))])))

(defview snt-amount-label []
  (letsubs [snt-amount [:get-in [:my-profile/tribute-to-talk :snt-amount]]]
    (let [snt-amount (or snt-amount "0")]
      [react/view {:style styles/snt-amount-container}
       [react/text {:style styles/snt-amount-label
                    :number-of-lines 1
                    :ellipsize-mode :middle}
        [react/text {:style styles/snt-amount} snt-amount]
        " SNT"]
       [snt-asset-value]])))

(defn- get-new-snt-amount
  [snt-amount numpad-symbol]
  ;; TODO: Put some logic in place so that incorrect numbers can not
  ;; be entered
  (let [snt-amount  (or snt-amount "0")]
    (if (= numpad-symbol :remove)
      (let [len (count snt-amount)
            s (subs snt-amount 0 (dec len))]
        (cond-> s
          ;; Remove both the digit after the dot and the dot itself
          (string/ends-with? s ".") (subs 0 (- len 2))
          ;; Set default value if last digit is removed
          (string/blank? s) (do "0")))
      (cond
        ;; Disallow two consecutive dots
        (and (string/includes? snt-amount ".") (= numpad-symbol "."))
        snt-amount
        ;; Disallow more than 2 digits after the dot
        (and (string/includes? snt-amount ".")
             (> (count (second (string/split snt-amount #"\."))) 1))
        snt-amount
        ;; Replace initial "0" by the first digit
        (and (= snt-amount "0") (not= numpad-symbol "."))
        (str numpad-symbol)
        :else (str snt-amount numpad-symbol)))))

(defn number-view
  [numpad-symbol {:keys [on-press]}]
  [react/touchable-opacity
   {:on-press #(on-press numpad-symbol)}
   [react/view {:style styles/number-container}
    (if (= numpad-symbol :remove)
      [icons/icon :main-icons/backspace {:color colors/blue}]
      [react/text {:style styles/number} numpad-symbol])]])

(defview number-row
  [[left middle right] opts]
  [react/view {:style styles/number-row}
   [number-view left opts]
   [react/view {:style styles/vertical-number-separator}]
   [number-view middle opts]
   [react/view {:style styles/vertical-number-separator}]
   [number-view right opts]])

(defview number-pad
  [opts]
  [react/view {:style styles/number-pad}
   [number-row [1 2 3] opts]
   [react/view {:style (styles/horizontal-separator 12 24)}]
   [number-row [4 5 6] opts]
   [react/view {:style (styles/horizontal-separator 12 24)}]
   [number-row [7 8 9] opts]
   [react/view {:style (styles/horizontal-separator 12 24)}]
   [number-row ["." 0 :remove] opts]])

(defview set-snt-amount []
  (letsubs [snt-amount [:get-in [:my-profile/tribute-to-talk :snt-amount]]]
    [react/scroll-view {:content-container-style  styles/set-snt-amount-container}
     [react/view {:style (styles/horizontal-separator 16 32)}]
     [snt-amount-label]
     [react/view {:style (styles/horizontal-separator 16 40)}]
     [number-pad {:on-press (fn [numpad-symbol]
                              (re-frame/dispatch [:set-in [:my-profile/tribute-to-talk :snt-amount]
                                                  (get-new-snt-amount snt-amount numpad-symbol)]))}]
     [react/i18n-text {:style (assoc styles/description-label :margin-horizontal 16)
                       :key   :tribute-to-talk-set-snt-amount}]]))

(defview personalized-message []
  (letsubs [{:keys [message]} [:my-profile/tribute-to-talk]]
    [react/scroll-view {:content-container-style styles/personalized-message-container}
     [react/view {:style styles/personalized-message-title}
      [react/text {:style (assoc styles/description-label
                                 :color colors/black)}
       (i18n/label :t/personalized-message)
       [react/text {:style styles/description-label}
        (str " (" (i18n/label :t/optional) ")")]]]
     [react/text-input (cond-> {:style (assoc styles/personalized-message-input :height 144
                                              :align-self :stretch)
                                :multiline true
                                :on-change-text #(re-frame/dispatch [:set-in [:my-profile/tribute-to-talk :message] %1])
                                :placeholder (i18n/label :t/tribute-to-talk-message-placeholder)}
                         (not (string/blank? message))
                         (assoc :default-value message))]
     [react/text {:style (assoc styles/description-label :margin-top 16 :margin-horizontal 32)}
      (i18n/label :t/tribute-to-talk-you-can-leave-a-message)]]))

(defview finish []
  (letsubs [amount [:get-in [:my-profile/tribute-to-talk :snt-amount]]]
    [react/view {:style styles/intro-container}
     [react/view {:style {:flex 3
                          :justify-content :center
                          :align-items :center}}
      [react/view {:style (styles/finish-circle (if amount
                                                  colors/green-transparent-10
                                                  colors/gray-lighter) 80)}
       [react/view {:style styles/finish-circle-with-shadow}
        [icons/icon :main-icons/check {:color (if amount colors/green colors/gray)}]]]]
     [react/view  {:style {:flex 1
                           :justify-content :center
                           :align-items :center}}
      [react/text {:style styles/finish-label} (i18n/label (if amount :t/you-are-all-set :t/tribute-to-talk-disabled))]
      [react/text {:style (assoc styles/description-label :margin-top 16)}
       (i18n/label (if amount :t/tribute-to-talk-finish-desc :t/tribute-to-talk-disabled-note))
       (when amount [react/text {:style (assoc styles/description-label :color colors/black)} amount])
       (when amount " SNT")]]]))

(defview enabled-note []
  [react/view {:style styles/enabled-note}
   [icons/icon :main-icons/info {:color colors/gray}]
   [react/view {:style {:margin-left 11}}
    [react/text {:style styles/enabled-note-text}
     (i18n/label :t/tribute-to-talk-enabled)]
    [react/text {:style (assoc styles/enabled-note-text :font-weight :normal)}
     (i18n/label :t/tribute-to-talk-add-friends)]]])

(defview edit []
  (letsubs [{:keys [snt-amount message]} [:my-profile/tribute-to-talk]]
    [react/scroll-view {:content-container-style styles/edit-container}
     [react/view
      [react/view {:style styles/edit-screen-top-row}
       [react/view {:style {:flex-direction  :row
                            :justify-content :flex-start
                            :align-items     :flex-start}}
        [react/view {:style (styles/icon-view colors/blue)}
         [icons/icon :icons/logo {:color colors/white :width 20 :height 20}]]
        [react/view {:style {:margin-left 16 :justify-content :flex-start}}
         [react/view {:style {:justify-content :center
                              :align-items :center}}
          [react/text {:style styles/current-snt-amount}
           snt-amount [react/text {:style (assoc styles/current-snt-amount :color colors/gray)} " SNT"]]]
         [snt-asset-value]]]
       [react/view {:style {:justify-content :center
                            :align-items :center}}
        [react/text {:on-press #(re-frame/dispatch [:tribute-to-talk/start-editing])
                     :style styles/edit-label}
         (i18n/label :t/edit)]]]
      (when-not (string/blank? message)
        [react/view {:style styles/edit-view-message-container}
         [react/text {:style styles/edit-view-message}
          message]])
      [separator]
      [react/text {:style styles/edit-note}
       (i18n/label :t/tribute-to-talk-you-require-snt)]

      [react/touchable-highlight {:on-press #(re-frame/dispatch [:tribute-to-talk/remove])
                                  :style styles/remove-view}
       [react/view {:style {:flex-direction :row}}
        [react/view {:style (styles/icon-view (colors/alpha colors/red 0.1))}
         [icons/icon :main-icons/logout {:color colors/red}]]
        [react/view  {:style {:justify-content :center
                              :align-items :center}}
         [react/text {:style styles/remove-text}
          (i18n/label :t/remove)]]]]
      [react/text {:style styles/remove-note}
       (i18n/label :t/tribute-to-talk-removing-note)]]

     [enabled-note]]))

(defview chat-sample []
  [react/view {:style (assoc styles/learn-more-section :margin-top 24)}
   [react/view {:style {:flex-direction :row
                        :align-items :center}}
    [react/view {:style {:background-color colors/white
                         :justify-content :center :align-items :center}}
     [icons/icon :tiny-icons/tribute-to-talk {:color colors/blue}]]
    [react/text {:style {:color colors/gray :font-size 13 :line-height 22 :margin-left 4}}
     (i18n/label :t/tribute-to-talk)]]
   [react/view {:style styles/chat-sample-bubble}
    [react/text {:style {:font-size 15 :color colors/black}}
     (i18n/label :t/tribute-to-talk-sample-text)]]
   [react/view {:style (assoc styles/chat-sample-bubble :width 141)}
    [react/text {:style {:font-size 22 :color colors/black}} "1000"
     [react/text {:style {:font-size 22 :color colors/gray}} " SNT"]]
    [react/text {:style {:font-size 12 :color colors/black}}
     "~3.48"
     [react/text {:style {:font-size 12 :color colors/gray}} " USD"]]
    [react/view {:style styles/pay-to-chat-container}
     [react/text {:style styles/pay-to-chat-text}
      (i18n/label :t/pay-to-chat)]]]])

(defview learn-more []
  [react/scroll-view {:content-container-style styles/learn-more-container}
   [components.common/image-contain {:container-style styles/learn-more-image}
    (:tribute-to-talk resources/ui)]

   [react/text {:style styles/learn-more-title-text}
    (i18n/label :t/tribute-to-talk)]
   [react/view {:style styles/learn-more-text-container-1}
    [react/text {:style styles/learn-more-text}
     (i18n/label :t/tribute-to-talk-learn-more-1)]]
   [separator]
   [chat-sample]
   [react/view {:style styles/learn-more-text-container-2}
    [react/text {:style styles/learn-more-text}
     (i18n/label :t/tribute-to-talk-learn-more-2)]]
   [react/view {:style (assoc styles/learn-more-section
                              :flex-direction     :row
                              :align-item         :flex-stretch
                              :padding-horizontal 16
                              :padding-vertical   12)}
    [react/view {:style (styles/icon-view colors/blue-light)}
     [icons/icon :main-icons/add-contact {:color colors/blue}]]
    [react/view {:style {:margin-left 16 :justify-content :center}}
     [react/text {:style (assoc styles/learn-more-text :color colors/blue)}
      (i18n/label :t/add-to-contacts)]]]
   [react/view {:style styles/learn-more-text-container-2}
    [react/text {:style styles/learn-more-text}
     (i18n/label :t/tribute-to-talk-learn-more-3)]]])

(defview tribute-to-talk []
  (letsubs [current-account           [:account/account]
            {:keys [step snt-amount editing?] :as tr-settings} [:my-profile/tribute-to-talk]]
    [react/keyboard-avoiding-view {:style styles/container}
     [react/safe-area-view {:style {:flex 1}}
      (re-frame/dispatch [:accounts.ui/tribute-to-talk-seen])
      [status-bar/status-bar]
      [toolbar/toolbar
       nil
       (when-not (= :finish step)
         (toolbar/nav-button (actions/back #(re-frame/dispatch
                                             [:tribute-to-talk/step-back step editing?]))))
       [react/view
        [react/text {:style styles/tribute-to-talk}
         (i18n/label :t/tribute-to-talk)]
        (when-not (#{:edit :learn-more} step)
          [react/text {:style styles/step-n}
           (if (= step :finish)
             (i18n/label (if snt-amount :t/completed :t/disabled))
             (i18n/label :t/step-i-of-n {:step ((steps-numbers editing?) step)
                                         :number (if editing? 2 3)}))])
        (when (= step :learn-more)
          [react/text {:style styles/step-n}
           (i18n/label :t/learn-more)])]]

      (case step
        :intro                [intro]
        :set-snt-amount       [set-snt-amount]
        :edit                 [edit]
        :learn-more           [learn-more]
        :personalized-message [personalized-message]
        :finish               [finish])

      (when-not (#{:learn-more :edit} step)
        [react/view {:style styles/bottom-toolbar}
         (let [disabled? (and (= step :set-snt-amount)
                              (or (string/blank? snt-amount)
                                  (= "0" snt-amount)
                                  (string/ends-with? snt-amount ".")))]
           [components.common/button {:button-style styles/intro-button
                                      :disabled?    disabled?
                                      :uppercase?   false
                                      :label-style  (when disabled? {:color colors/gray})
                                      :on-press     #(re-frame/dispatch
                                                      [:tribute-to-talk/step-forward tr-settings])
                                      :label        (i18n/label (step-forward-label step))}])])]]))
