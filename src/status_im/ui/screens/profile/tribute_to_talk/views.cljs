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
   :set-snt-amount       (if editing? 1 2)})

(def step-forward-label
  {:intro                :t/get-started
   :set-snt-amount       :t/continue
   :finish               :t/ok-got-it})

(defn intro
  []
  [react/scroll-view
   [react/view {:style styles/intro-container}
    [react/view {:style {:flex       1
                         :min-height 32}}]

    [react/image {:source (resources/get-image :tribute-to-talk)
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
       (i18n/label :t/learn-more)]]]]])

(defn snt-asset-value
  [fiat-value]
  [react/text {:style styles/snt-asset-value}
   fiat-value])

(defn snt-amount-label
  [snt-amount fiat-value]
  [react/view {:style styles/snt-amount-container}
   [react/nested-text {:style styles/snt-amount-label
                       :number-of-lines 1
                       :ellipsize-mode :middle}
    [{:style styles/snt-amount} (or snt-amount "0")]
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

(defn finish
  [snt-amount state]
  [react/view {:style styles/intro-container}
   [react/view {:style {:flex       1
                        :min-height 32}}]
   [react/view {:style {:justify-content :center
                        :align-items :center}}
    [react/view {:style (styles/finish-circle
                         (case state
                           :completed
                           colors/green-transparent-10
                           :disabled
                           colors/gray-lighter
                           :pending
                           colors/gray-lighter
                           :signing
                           colors/gray-lighter
                           :transaction-failed
                           colors/red-transparent-10)
                         80)}
     [react/view {:style styles/finish-circle-with-shadow}
      (if (#{:signing :pending} state)
        [react/activity-indicator {:animating true
                                   :size      :large
                                   :color colors/gray}]
        [icons/icon (case state
                      :completed :main-icons/check
                      :disabled :main-icons/cancel
                      :transaction-failed :main-icons/warning)
         {:width 48 :height 48
          :color (case state
                   :completed
                   colors/green
                   :disabled
                   colors/gray
                   :pending
                   colors/gray
                   :signing
                   colors/gray
                   :transaction-failed
                   colors/red)}])]]]

   [react/view {:style {:flex       1
                        :min-height 32}}]
   [react/view  {:style {:justify-content :center
                         :align-items     :center
                         :margin-bottom   32}}
    [react/text {:style styles/finish-label}
     (i18n/label (case state
                   :completed
                   :t/you-are-all-set
                   :disabled
                   :t/tribute-to-talk-disabled
                   :pending
                   :t/tribute-to-talk-pending
                   :signing
                   :t/tribute-to-talk-signing
                   :transaction-failed
                   :t/transaction-failed))]
    (case state
      :completed
      [react/nested-text {:style (assoc styles/description-label :margin-top 16)}
       (i18n/label :t/tribute-to-talk-finish-desc)
       [{:style {:color colors/black
                 :font-weight "600"}} snt-amount]
       " SNT"]
      :disabled
      [react/text {:style (assoc styles/description-label :margin-top 16)}
       (i18n/label :t/tribute-to-talk-disabled-note)]
      :pending
      [react/text {:style (assoc styles/description-label :margin-top 16)}
       (i18n/label :t/tribute-to-talk-pending-note)]
      :signing
      nil
      :transaction-failed
      [react/text {:style (assoc styles/description-label :margin-top 16)}
       (i18n/label :t/tribute-to-talk-transaction-failed-note)])]])

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
  [snt-amount fiat-value]
  [react/scroll-view {:content-container-style styles/edit-container}
   [react/view {:style styles/edit-screen-top-row}
    [react/view {:style {:flex-direction  :row
                         :justify-content :flex-start
                         :align-items     :flex-start}}
     [icons/icon :icons/logo {:container-style (styles/icon-view colors/blue)
                              :width           40
                              :height          40}]
     [react/view {:style {:margin-left 16 :justify-content :flex-start}}
      [react/view {:style {:justify-content :center
                           :align-items :center}}
       [react/nested-text {:style styles/current-snt-amount}
        snt-amount
        [{:style (assoc styles/current-snt-amount :color colors/gray)}
         " SNT"]]]
      [snt-asset-value fiat-value]]]
    [react/view {:flex 1}]
    [react/text {:on-press #(re-frame/dispatch
                             [:tribute-to-talk.ui/edit-pressed])
                 :style styles/edit-label}
     (i18n/label :t/edit)]]
   [separator]
   [react/text {:style styles/edit-note}
    (i18n/label :t/tribute-to-talk-you-require-snt)]

   [react/touchable-highlight {:on-press #(re-frame/dispatch
                                           [:tribute-to-talk.ui/remove-pressed])
                               :style styles/remove-view}
    [react/view {:style {:flex-direction :row}}
     [react/view {:style (styles/icon-view colors/red-transparent-10)}
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

(defn pay-to-chat-message
  [{:keys [snt-amount style public-key tribute-status
           tribute-label fiat-amount fiat-currency token]}]
  [react/view {:style style}
   [react/view {:style {:flex-direction :row
                        :align-items :center}}
    [react/view {:style {:background-color colors/white
                         :justify-content :center
                         :align-items :center}}
     [icons/tiny-icon :tiny-icons/tribute-to-talk {:color colors/blue}]]
    [react/text {:style {:color       colors/gray
                         :font-size   13
                         :margin-left 4}}
     (i18n/label :t/tribute-to-talk)]]
   (when snt-amount
     [react/view {:style styles/pay-to-chat-bubble}
      [react/nested-text {:style {:font-size 22}}
       (str snt-amount)
       [{:style {:font-size 22 :color colors/gray}} token]]
      [react/nested-text
       {:style {:font-size 12}}
       (str "~" fiat-amount)
       [{:style {:font-size 12 :color colors/gray}}
        (str " " fiat-currency)]]
      (if (or (nil? public-key) (= tribute-status :required))
        [react/view {:style styles/pay-to-chat-container}
         [react/text (cond-> {:style styles/pay-to-chat-text}
                       public-key
                       (assoc :on-press
                              #(re-frame/dispatch [:tribute-to-talk.ui/on-pay-to-chat-pressed
                                                   public-key])))
          (i18n/label :t/pay-to-chat)]]
        [react/view {:style styles/pay-to-chat-container}
         [react/view {:style (styles/payment-status-icon (= tribute-status :pending))}
          [icons/tiny-icon (if (= tribute-status :pending) :tiny-icons/tiny-pending :tiny-icons/tiny-check)
           {:color (if (= tribute-status :pending) colors/black colors/white)}]]
         [react/text {:style styles/payment-status-text} tribute-label]])])])

(defn learn-more [owner?]
  [react/view {:flex 1}
   (when-not owner?
     [toolbar/toolbar nil toolbar/default-nav-close
      [react/view
       [react/text {:style styles/tribute-to-talk}
        (i18n/label :t/tribute-to-talk)]
       [react/text {:style styles/step-n}
        (i18n/label :t/learn-more)]]])
   [react/scroll-view {:content-container-style styles/learn-more-container}
    [react/image {:source (resources/get-image :tribute-to-talk)
                  :style styles/learn-more-image}]
    [react/text {:style styles/learn-more-title-text}
     (i18n/label :t/tribute-to-talk)]
    [react/view {:style styles/learn-more-text-container-1}
     [react/text {:style styles/learn-more-text}
      (i18n/label (if owner? :t/tribute-to-talk-learn-more-1
                      :t/tribute-to-talk-paywall-learn-more-1))]]
    [separator]
    [pay-to-chat-message {:snt-amount 1000
                          :token " SNT"
                          :fiat-currency "USD"
                          :fiat-amount "5"
                          :style (assoc styles/learn-more-section
                                        :align-items :flex-start
                                        :margin-top 24)}]
    [react/view {:style styles/learn-more-text-container-2}
     [react/text {:style styles/learn-more-text}
      (i18n/label (if owner? :t/tribute-to-talk-learn-more-2
                      :t/tribute-to-talk-paywall-learn-more-2))]]
    [react/view {:style (assoc styles/learn-more-section
                               :flex-direction     :row
                               :align-items        :stretch
                               :padding-horizontal 16
                               :padding-vertical   12)}
     [react/view {:style (styles/icon-view colors/blue-light)}
      [icons/icon :main-icons/add-contact {:color colors/blue}]]
     [react/view {:style {:margin-left 16 :justify-content :center}}
      [react/text {:style (assoc styles/learn-more-text :color colors/blue)}
       (i18n/label (if owner? :t/add-to-contacts :t/share-profile))]]]
    [react/view {:style styles/learn-more-text-container-2}
     [react/text {:style styles/learn-more-text}
      (i18n/label (if owner? :t/tribute-to-talk-learn-more-3
                      :t/tribute-to-talk-paywall-learn-more-3))]]]])

(defview tribute-to-talk []
  (letsubs [{:keys [step snt-amount editing?
                    fiat-value disable-button? state]}
            [:tribute-to-talk/settings-ui]]
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
             (i18n/label (case state
                           :completed :t/completed
                           :pending :t/pending
                           :signing :t/signing
                           :transaction-failed :t/transaction-failed
                           :disabled :t/disabled))
             (i18n/label :t/step-i-of-n {:step ((steps-numbers editing?) step)
                                         :number (if editing? 2 3)}))])
        (when (= step :learn-more)
          [react/text {:style styles/step-n}
           (i18n/label :t/learn-more)])]]

      (case step
        :intro                [intro]
        :set-snt-amount       [set-snt-amount snt-amount]
        :edit                 [edit snt-amount fiat-value]
        :learn-more           [learn-more step]
        :finish               [finish snt-amount state])

      (when-not (#{:learn-more :edit} step)
        [react/view {:style styles/bottom-toolbar}
         [components.common/button {:button-style styles/intro-button
                                    :disabled?    disable-button?
                                    :label-style  (when disable-button? {:color colors/gray})
                                    :on-press     #(re-frame/dispatch
                                                    [:tribute-to-talk.ui/step-forward-pressed])
                                    :label        (i18n/label (step-forward-label step))}]])]]))
