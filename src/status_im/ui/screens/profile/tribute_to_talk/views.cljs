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
  [react/view {:style {;:flex 1 
                       :height 1 :background-color colors/gray-lighter}}])

(defn steps-numbers [editing?]
  {:intro                1
   :set-snt-amount       (if editing? 1 2)
   :personalized-message (if editing? 2 3)
   :finish               3})

(defn step-back [step editing?]
  (re-frame/dispatch
   (case step
     (:intro :edit)                [:navigate-back]
     (:learn-more :set-snt-amount) [:set-in [:my-profile/tribute-to-talk :step]
                                    (if editing? :edit :intro)]
     :personalized-message         [:set-in [:my-profile/tribute-to-talk :step] :set-snt-amount]
     :finish                       [:set-in [:my-profile/tribute-to-talk :step] :personalized-message])))
(def step-forward-label
  {:intro :t/get-started
   :set-snt-amount :t/continue
   :personalized-message :t/tribute-to-talk-sign-and-set-tribute
   :finish  :t/ok-got-it})

(defn step-forward [step editing?]
  (re-frame/dispatch
   (case step
     :intro [:set-in [:my-profile/tribute-to-talk :step] :set-snt-amount]
     :set-snt-amount [:set-in [:my-profile/tribute-to-talk :step] :personalized-message]
     :personalized-message [:set-in [:my-profile/tribute-to-talk :step] (if editing? :edit :finish)]       :finish [:navigate-back])))

(defn intro []
  [react/view {:style styles/intro-container}
   [components.common/image-contain {:container-style styles/intro-image}
    (:tribute-to-talk resources/ui)]
   [react/view
    [react/i18n-text {:style styles/intro-text
                      :key   :tribute-to-talk}]
    [react/i18n-text {:style (assoc styles/description-label :margin-top 12)
                      :key   :tribute-to-talk-desc}]
    [react/view {:style {:height 52
                         :padding-horizontal 32
                         :margin-bottom 16
                         :align-items :center
                         :justify-content :center}}
     [react/text {:style  {:font-size 15 :line-height 21 :color colors/blue}
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

(defview number-view [numpad-symbol]
  (letsubs [snt-amount [:get-in [:my-profile/tribute-to-talk :snt-amount]]]
    (let [snt-amount (or snt-amount "0")
          ;; Put some logic in place so that incorrect numbers can not
          ;; be entered
          new-snt-amount (if (= numpad-symbol :remove)
                           (let [len (count snt-amount)
                                 s (subs snt-amount 0 (dec len))]
                             ;; Remove both the digit after the dot and the dot itself
                             (if (string/ends-with? s ".")
                               (subs s 0 (- len 2))
                               s))
                           (cond (and (string/includes? snt-amount ".") (= numpad-symbol "."))
                                 ;; Disallow two consecutive dots
                                 snt-amount

                                 (and (string/includes? snt-amount ".")
                                      (> (count (second (string/split snt-amount #"\."))) 1))
                                 ;; Disallow more than 2 digits after the dot
                                 snt-amount

                                 (and (= snt-amount "0") (not= numpad-symbol "."))
                                 ;; Replace initial "0" by the first digit
                                 (str numpad-symbol)
                                 :else (str snt-amount numpad-symbol)))
          new-snt-amount (if (string/blank? new-snt-amount) "0" new-snt-amount)]
      [react/touchable-opacity
       {:on-press #(re-frame/dispatch [:set-in [:my-profile/tribute-to-talk :snt-amount]
                                       new-snt-amount])}
       [react/view {:style styles/number-container}
        (if (= numpad-symbol :remove)
          [icons/icon :main-icons/backspace {:color colors/blue}]
          [react/text {:style styles/number} numpad-symbol])]])))

(defview number-row [elements]
  [react/view {:style {:flex 1
                       :flex-direction :row :justify-content :space-around}}
   elements])

(defview number-pad []
  [react/view {:style {:flex 1
                       :justify-content :space-around}}
   (->> (into (vec (range 1 10))
              ["." 0 :remove])
        (map (fn [n] ^{:key n} [number-view n]))
        (partition 3)
        (mapv (fn [elements]
                ^{:key elements} [number-row elements]))
        seq)])

(defview set-snt-amount []
  (letsubs [snt-amount [:get-in [:my-profile/tribute-to-talk :snt-amount]]]
    [react/scroll-view {:content-container-style (assoc styles/intro-container :justify-content :flex-start
                                                        :align-items :stretch)}
     [snt-amount-label]
     [react/view {:style {:flex 1}}
      [number-pad]
      [react/i18n-text {:style (assoc styles/description-label :margin-top 18)
                        :key   :tribute-to-talk-set-snt-amount}]]]))

(defview personalized-message []
  (letsubs [{:keys [message]} [:my-profile/tribute-to-talk]]
    [react/scroll-view {:content-container-style (assoc styles/intro-container :margin-horizontal 16
                                                        :justify-content :flex-start)}
     [react/view {:style {:margin-top 24
                          :margin-bottom 10
                          :align-self :flex-start}}
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
       [react/view {:style (assoc (styles/finish-circle colors/white 40)
                                  :elevation 5
                                  :shadow-offset {:width 0 :height 2}
                                  :shadow-radius 4
                                  :shadow-color (colors/alpha "#435971" 0.124066))}
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
    [react/scroll-view {:content-container-style (assoc styles/intro-container
                                                        :margin-horizontal 0)}
     [react/view #_{:style {:flex 1}}
      [react/view {:style styles/edit-screen-top-row}
       [react/view {:style {:flex-direction :row
                            :justify-content :flex-start
                            :align-items :flex-start}}
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
        [react/text {:on-press #(do
                                  (re-frame/dispatch [:set-in [:my-profile/tribute-to-talk :step] :set-snt-amount])
                                  (re-frame/dispatch [:set-in [:my-profile/tribute-to-talk :editing?] true]))
                     :style styles/edit-label}
         (i18n/label :t/edit)]]]
      [react/text-input {:style styles/edit-view-message
                         :multiline true
                         :editable false
                         :placeholder ""
                         :default-value message}]
      [separator]
      [react/text {:style {:font-size 15
                           :color colors/gray
                           :margin-top 16
                           :margin-horizontal 16
                           :text-align :center}}
       (i18n/label :t/tribute-to-talk-you-require-snt)]

      [react/touchable-highlight {:on-press #(do
                                               (re-frame/dispatch [:set-in [:my-profile/tribute-to-talk :snt-amount] nil])
                                               (re-frame/dispatch [:set-in [:my-profile/tribute-to-talk :step] :finish]))
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
                         ;:width 16 :height 16
                         :justify-content :center :align-items :center
                         ;:border-radius 4
}}
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
    [react/view {:style {:align-items :center :margin-top 19}}
     [react/text {:style {:font-size 15 :color colors/blue}}
      (i18n/label :t/pay-to-chat)]]]])

(defview learn-more []
  [react/scroll-view {:content-container-style styles/learn-more-container}
   [components.common/image-contain {:container-style styles/learn-more-image}
    (:tribute-to-talk resources/ui)]

   [react/text {:style styles/learn-more-title-text}
    (i18n/label :t/tribute-to-talk)]
   [react/view {:style {:margin-horizontal 32
                        :margin-top 12
                        :margin-bottom 24}}
    [react/text {:style styles/learn-more-text}
     (i18n/label :t/tribute-to-talk-learn-more-1)]]
   [separator]
   [chat-sample]
   [react/view {:style {:margin-horizontal 32
                        :margin-top 16
                        :margin-bottom 32}}
    [react/text {:style styles/learn-more-text}
     (i18n/label :t/tribute-to-talk-learn-more-2)]]
   [react/view {:style (assoc styles/learn-more-section
                              :flex-direction :row
                              :align-item :flex-stretch
                              :padding-horizontal 16
                              :padding-vertical 12)}
    [react/view {:style (styles/icon-view colors/blue-light)}
     [icons/icon :main-icons/add-contact {:color colors/blue :width 20 :height 20}]]

    [react/view {:style {:margin-left 16 :justify-content :center}}
     [react/text {:style (assoc styles/learn-more-text :color colors/blue)}
      (i18n/label :t/add-to-contacts)]]]
   [react/view {:style {:margin-top 16 :margin-horizontal 32}}
    [react/text {:style styles/learn-more-text}
     (i18n/label :t/tribute-to-talk-learn-more-3)]]])

(defview tribute-to-talk []
  (letsubs [current-account [:account/account]
            {:keys [step snt-amount]} [:my-profile/tribute-to-talk]
            editing? [:get-in [:my-profile/tribute-to-talk :editing?]]]
    [react/keyboard-avoiding-view {:style styles/container}
     [status-bar/status-bar]
     [toolbar/toolbar
      nil
      (when-not (= :finish step)
        (toolbar/nav-button (actions/back #(step-back step editing?))))
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
       :intro [intro]
       :set-snt-amount [set-snt-amount]
       :edit [edit]
       :learn-more [learn-more]
       :personalized-message [personalized-message]
       :finish [finish])
     (when-not (#{:learn-more :edit} step)
       [separator]
       [react/view {:style styles/bottom-toolbar}
        (let [disabled? (and (= step :set-snt-amount)
                             (or (= "0" snt-amount)
                                 (string/blank? snt-amount)))]
          [components.common/button {:button-style styles/intro-button
                                     :disabled? disabled?
                                     :uppercase? false
                                     :label-style (when disabled? {:color colors/gray})
                                     :on-press     #(step-forward step editing?)
                                     :label        (i18n/label (step-forward-label step))}])])]))
