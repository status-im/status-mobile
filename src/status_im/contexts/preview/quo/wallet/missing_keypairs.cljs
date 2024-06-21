(ns status-im.contexts.preview.quo.wallet.missing-keypairs
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def keypair
  {:key-uid "0x01"
   :type    "seed"
   :name    "My Key Pair"
   :blur?   false})

(def accounts
  [{:customization-color :turquoise
    :emoji               "\uD83C\uDFB2"
    :type                :default}
   {:customization-color :purple
    :emoji               "\uD83C\uDF7F"
    :type                :default}
   {:customization-color :army
    :emoji               "\uD83D\uDCC8"
    :type                :default}
   {:customization-color :orange
    :emoji               "\uD83C\uDFF0"
    :type                :default}
   {:customization-color :yellow
    :emoji               "\uD83C\uDFDD️"
    :type                :default}])

(def descriptor
  [{:key :blur? :type :boolean}])

(def component-props
  {:blur?    false
   :keypairs [{:name     (:name keypair)
               :key-uid  (:key-uid keypair)
               :type     (keyword (:type keypair))
               :accounts accounts}]})

(defn view
  []
  (let [state (reagent/atom {:blur? false})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :blur?                     (:blur? @state)
        :show-blur-background?     true
        :blur-dark-only?           true
        :blur-height               400
        :component-container-style {:padding-vertical 30
                                    :flex-direction   :row
                                    :justify-content  :center}}
       [rn/view {:style {:flex 1}}
        [quo/missing-keypairs
         (merge component-props @state)]]])))
