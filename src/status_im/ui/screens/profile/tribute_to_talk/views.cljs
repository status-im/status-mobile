(ns status-im.ui.screens.profile.tribute-to-talk.views
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.profile.tribute-to-talk.styles :as styles])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

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

(defn intro
  []
  [react/view {:style styles/intro-container}
   [react/view {:style {:flex       1
                        :min-height 32}}]

   [react/image {:source (:tribute-to-talk resources/ui)
                 :style styles/intro-image}]
   [react/view {:style {:flex       1
                        :min-height 32}}]

   [react/view
    [react/i18n-text {:style styles/intro-text
                      :key   :tribute-to-talk}]
    [react/i18n-text {:style (assoc styles/description-label :margin-top 12)
                      :key   :tribute-to-talk-desc}]
    [react/view {:style styles/learn-more-link}
     [react/text {:style styles/learn-more-link-text
                  :on-press #(re-frame/dispatch
                              [:tribute-to-talk.ui/learn-more-pressed])}
      (i18n/label :t/learn-more)]]]])

(defn snt-asset-value
  [fiat-value]
  [react/text {:style styles/snt-asset-value}
   fiat-value])

(defn snt-amount-label
  [snt-amount fiat-value]
  [react/view {:style styles/snt-amount-container}
   [react/text {:style styles/snt-amount-label
                :number-of-lines 1
                :ellipsize-mode :middle}
    [react/text {:style styles/snt-amount} (or snt-amount "0")]
    " SNT"]
   [snt-asset-value fiat-value]])

(defn number-view
  [numpad-symbol {:keys [on-press]}]
  [react/touchable-opacity
   {:on-press #(on-press numpad-symbol)}
   [react/view {:style styles/number-container}
    (if (= numpad-symbol :remove)
      [icons/icon :main-icons/backspace {:color colors/blue}]
      [react/text {:style styles/number} numpad-symbol])]])

(defn number-row
  [[left middle right] opts]
  [react/view {:style styles/number-row}
   [number-view left opts]
   [react/view {:style styles/vertical-number-separator}]
   [number-view middle opts]
   [react/view {:style styles/vertical-number-separator}]
   [number-view right opts]])

(defn number-pad
  [opts]
  [react/view {:style styles/number-pad}
   [number-row [1 2 3] opts]
   [react/view {:style (styles/horizontal-separator 12 24)}]
   [number-row [4 5 6] opts]
   [react/view {:style (styles/horizontal-separator 12 24)}]
   [number-row [7 8 9] opts]
   [react/view {:style (styles/horizontal-separator 12 24)}]
   [number-row ["." 0 :remove] opts]])

(defn set-snt-amount
  [snt-amount]
  [react/scroll-view
   {:content-container-style  styles/set-snt-amount-container}
   [react/view {:style (styles/horizontal-separator 16 32)}]
   [snt-amount-label snt-amount]
   [react/view {:style (styles/horizontal-separator 16 40)}]
   [number-pad {:on-press (fn [numpad-symbol]
                            (re-frame/dispatch
                             [:tribute-to-talk.ui/numpad-key-pressed numpad-symbol]))}]
   [react/i18n-text {:style (assoc styles/description-label :margin-horizontal 16)
                     :key   :tribute-to-talk-set-snt-amount}]])

(defn personalized-message
  [message]
  [react/scroll-view
   {:content-container-style styles/personalized-message-container}
   [react/view {:style styles/personalized-message-title}
    [react/text {:style {:text-align :center}}
     (i18n/label :t/personalized-message)
     [react/text {:style styles/description-label}
      (str " (" (i18n/label :t/optional) ")")]]]
   [react/text-input
    (cond-> {:style (assoc styles/personalized-message-input :height 144
                           :align-self :stretch)
             :multiline true
             :on-change-text #(re-frame/dispatch
                               [:tribute-to-talk.ui/message-changed %1])
             :placeholder (i18n/label :t/tribute-to-talk-message-placeholder)}
      (not (string/blank? message))
      (assoc :default-value message))]
   [react/text {:style (assoc styles/description-label :margin-top 16 :margin-horizontal 32)}
    (i18n/label :t/tribute-to-talk-you-can-leave-a-message)]])

(defn finish
  [snt-amount]
  [react/view {:style styles/intro-container}
   [react/view {:style {:flex       1
                        :min-height 32}}]
   [react/view {:style {:justify-content :center
                        :align-items :center}}
    [react/view {:style (styles/finish-circle (if snt-amount
                                                colors/green-transparent-10
                                                colors/gray-lighter) 80)}
     [react/view {:style styles/finish-circle-with-shadow}
      [icons/icon :main-icons/check {:color (if snt-amount
                                              colors/green
                                              colors/gray)}]]]]

   [react/view {:style {:flex       1
                        :min-height 32}}]
   [react/view  {:style {:justify-content :center
                         :align-items     :center
                         :margin-bottom   32}}
    [react/text {:style styles/finish-label}
     (i18n/label (if snt-amount
                   :t/you-are-all-set
                   :t/tribute-to-talk-disabled))]
    (if snt-amount
      [react/text {:style (assoc styles/description-label :margin-top 16)}
       (i18n/label :t/tribute-to-talk-finish-desc)
       [react/text {:style {:text-align :center}}
        snt-amount]
       " SNT"]
      [react/text {:style (assoc styles/description-label :margin-top 16)}
       (i18n/label :t/tribute-to-talk-disabled-note)])]])

(defn enabled-note
  []
  [react/view {:style styles/enabled-note}
   [react/view {:flex-direction :row}
    [icons/icon :main-icons/info {:color colors/gray}]
    [react/text {:style (assoc styles/enabled-note-text
                               :margin-left 11)}
     (i18n/label :t/tribute-to-talk-enabled)]]
   [react/text {:style (assoc styles/enabled-note-text
                              :margin-left 35)}
    (i18n/label :t/tribute-to-talk-add-friends)]])

(defn edit
  [snt-amount message fiat-value]
  [react/scroll-view {:content-container-style styles/edit-container}
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
        snt-amount
        [react/text {:style (assoc styles/current-snt-amount
                                   :color colors/gray)} " SNT"]]]
      [snt-asset-value fiat-value]]]
    [react/view {:flex 1}]
    [react/text {:on-press #(re-frame/dispatch
                             [:tribute-to-talk.ui/edit-pressed])
                 :style styles/edit-label}
     (i18n/label :t/edit)]]
   (when-not (string/blank? message)
     [react/view {:flex-direction :row
                  :margin-bottom  16}
      [react/view {:style styles/edit-view-message-container}
       [react/text message]]
      [react/view {:flex 1}]])
   [separator]
   [react/text {:style styles/edit-note}
    (i18n/label :t/tribute-to-talk-you-require-snt)]

   [react/touchable-highlight {:on-press #(re-frame/dispatch
                                           [:tribute-to-talk.ui/remove-pressed])
                               :style styles/remove-view}
    [react/view {:style {:flex-direction :row}}
     [react/view {:style (styles/icon-view (colors/alpha colors/red 0.1))}
      [icons/icon :main-icons/logout {:color colors/red}]]
     [react/view  {:style {:justify-content :center
                           :align-items :center}}
      [react/text {:style styles/remove-text}
       (i18n/label :t/remove)]]]]
   [react/text {:style styles/remove-note}
    (i18n/label :t/tribute-to-talk-removing-note)]
   [react/view {:flex       1
                :min-height 24}]
   [enabled-note]])

(defn chat-sample []
  [react/view {:style (assoc styles/learn-more-section :margin-top 24)}
   [react/view {:style {:flex-direction :row
                        :align-items :center}}
    [react/view {:style {:background-color colors/white
                         :justify-content :center :align-items :center}}
     [icons/icon :tiny-icons/tribute-to-talk {:color colors/blue}]]
    [react/text {:style {:color       colors/gray
                         :font-size   13
                         :margin-left 4}}
     (i18n/label :t/tribute-to-talk)]]
   [react/view {:style styles/chat-sample-bubble}
    [react/text (i18n/label :t/tribute-to-talk-sample-text)]]
   [react/view {:style (assoc styles/chat-sample-bubble :width 141)}
    ;;TODO replace hardcoded values
    [react/text {:style {:font-size 22}} "1000"
     [react/text {:style {:font-size 22 :color colors/gray}} " SNT"]]
    [react/text {:style {:font-size 12}}
     "~3.48"
     [react/text {:style {:font-size 12 :color colors/gray}} " USD"]]
    [react/view {:style styles/pay-to-chat-container}
     [react/text {:style styles/pay-to-chat-text}
      (i18n/label :t/pay-to-chat)]]]])

(defn learn-more []
  [react/scroll-view {:content-container-style styles/learn-more-container}
   [react/image {:source (:tribute-to-talk resources/ui)
                 :style styles/learn-more-image}]
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
            {:keys [step snt-amount editing? message fiat-value disabled?]}
            [:tribute-to-talk/ui]]
    [react/keyboard-avoiding-view {:style styles/container}
     [react/safe-area-view {:style {:flex 1}}
      [status-bar/status-bar]
      [toolbar/toolbar
       nil
       (when-not (= :finish step)
         (toolbar/nav-button
          (actions/back #(re-frame/dispatch
                          [:tribute-to-talk.ui/step-back-pressed]))))
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
        :set-snt-amount       [set-snt-amount snt-amount]
        :edit                 [edit snt-amount message fiat-value]
        :learn-more           [learn-more]
        :personalized-message [personalized-message message]
        :finish               [finish snt-amount])

      (when-not (#{:learn-more :edit} step)
        [react/view {:style styles/bottom-toolbar}
         [components.common/button {:button-style styles/intro-button
                                    :disabled?    disabled?
                                    :label-style  (when disabled? {:color colors/gray})
                                    :on-press     #(re-frame/dispatch
                                                    [:tribute-to-talk.ui/step-forward-pressed])
                                    :label        (i18n/label (step-forward-label step))}]])]]))
