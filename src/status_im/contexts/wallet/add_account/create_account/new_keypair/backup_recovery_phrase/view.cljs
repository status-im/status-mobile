(ns status-im.contexts.wallet.add-account.create-account.new-keypair.backup-recovery-phrase.view
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.wallet.add-account.create-account.new-keypair.backup-recovery-phrase.style :as
     style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- word-item
  [item index _ increment]
  [rn/view {:style style/word-item}
   [quo/counter {:type :grey} (+ index increment)]
   [quo/text {:style {:margin-left 4}} item]])

(defn- words-column
  [{:keys [words first-half?]}]
  [rn/flat-list
   {:style          {:padding-vertical 8}
    :data           (if first-half? (subvec words 0 6) (subvec words 6))
    :separator      [rn/view {:style {:height 12}}]
    :render-fn      word-item
    :render-data    (if first-half? 1 7)
    :scroll-enabled false}])

(defn- step-item
  [item index _ {:keys [checked? customization-color]}]
  [rn/view {:style style/step-item}
   [quo/selectors
    {:type                :checkbox
     :customization-color customization-color
     :on-change           #(swap! checked? assoc (keyword (str index)) %)}]
   [quo/text {:style {:margin-left 12}} (i18n/label item)]])

(defn view
  []
  (let [step-labels         [:t/backup-step-1 :t/backup-step-2 :t/backup-step-3
                             :t/backup-step-4]
        checked?            (reagent/atom
                             {:0 false
                              :1 false
                              :2 false
                              :3 false})
        revealed?           (reagent/atom false)
        customization-color (rf/sub [:profile/customization-color])
        secret-phrase       (reagent/atom [])
        random-phrase       (reagent/atom [])]
    (fn []
      (let [theme (quo.theme/use-theme)]

        (rn/use-mount
         (fn []
           (native-module/get-random-mnemonic #(reset! secret-phrase (string/split % #"\s")))
           (native-module/get-random-mnemonic #(reset! random-phrase (string/split % #"\s")))))
        [rn/view {:style {:flex 1}}
         [quo/page-nav
          {:icon-name           :i/close
           :on-press            #(rf/dispatch [:navigate-back])
           :accessibility-label :top-bar}]
         [quo/page-top
          {:title            (i18n/label :t/backup-recovery-phrase)
           :description      :text
           :description-text (i18n/label :t/backup-recovery-phrase-description)
           :container-style  {:padding-bottom 8}}]
         [rn/view {:style (style/seed-phrase-container theme)}
          (when (pos? (count @secret-phrase))
            [:<>
             [words-column
              {:words       @secret-phrase
               :first-half? true}]
             [rn/view {:style (style/separator theme)}]
             [words-column
              {:words       @secret-phrase
               :first-half? false}]])
          (when-not @revealed?
            [rn/view {:style style/blur-container}
             [blur/view (style/blur theme)]])]
         (when-not @revealed?
           [rn/view
            {:style {:padding-horizontal 20
                     :padding-top        20}}
            [quo/text
             {:weight :semi-bold
              :style  {:margin-bottom 8}}
             (i18n/label :t/how-to-backup)]
            [rn/flat-list
             {:data           step-labels
              :render-fn      step-item
              :render-data    {:checked?            checked?
                               :customization-color customization-color}
              :scroll-enabled false}]])
         (if @revealed?
           [rn/view {:style style/slide-button}
            [quo/bottom-actions
             {:actions          :one-action
              :button-one-label (i18n/label :t/i-have-written)
              :button-one-props {:disabled?           (some false? (vals @checked?))
                                 :customization-color customization-color
                                 :on-press            #(rf/dispatch [:wallet/store-secret-phrase
                                                                     {:secret-phrase @secret-phrase
                                                                      :random-phrase @random-phrase}])}}]
            [quo/text
             {:size  :paragraph-2
              :style (style/description-text theme)}
             (i18n/label :t/next-you-will)]]
           [quo/bottom-actions
            {:actions          :one-action
             :button-one-label (i18n/label :t/reveal-phrase)
             :button-one-props {:disabled?           (some false? (vals @checked?))
                                :customization-color customization-color
                                :on-press            #(reset! revealed? true)}
             :container-style  style/slide-button}])]))))
