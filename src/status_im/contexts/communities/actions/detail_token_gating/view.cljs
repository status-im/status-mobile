(ns status-im.contexts.communities.actions.detail-token-gating.view
  (:require
    [quo.core :as quo]
    [re-frame.db :as re-frame.db]
    [react-native.core :as rn]
    [status-im.common.resources :as resources]
    [status-im.contexts.communities.actions.detail-token-gating.style :as style]
    [status-im.contexts.communities.utils :as communities.utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def mock-token
  [[{:symbol      "STT"
     :sufficient? true
     :loading?    false
     :amount      "12"
     :img-src     (resources/get-mock-image :status-logo)}
    {:symbol      "ETH"
     :sufficient? true
     :loading?    false
     :amount      "13"
     :img-src     (resources/get-mock-image :ethereum-address)}
    {:symbol      "STT"
     :sufficient? false
     :loading?    false
     :amount      "13"
     :img-src     (resources/get-mock-image :status-logo)}
    {:symbol      "STT"
     :sufficient? true
     :loading?    false
     :amount      "1"
     :img-src     (resources/get-mock-image :status-logo)}
    {:symbol      "STT"
     :sufficient? true
     :loading?    false
     :amount      "10"
     :img-src     (resources/get-mock-image :status-logo)}]])
(def mock-highest-permission-role 1)
(def mock-full-permission-map
  {:can-request-access? true
   :highest-permission-role 5
   :networks-not-supported? false
   :no-member-permission? false
   :tokens
   {:symbol "TMPER"
    :sufficient? true
    :loading? false
    :amount "1"
    :img-src
    "data:image/jpeg;base64,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"}})
(def token-permission
  [["1cfb5618-5e6a-4695-a91c-19e7e4f0af3a"
    {:id "1cfb5618-5e6a-4695-a91c-19e7e4f0af3a"
     :type 2
     :token_criteria
     [{:contract_addresses
       {:5        "0x3d6afaa395c31fcd391fe3d562e75fe9e8ec7e6a"
        :11155111 "0xe452027cdef746c7cd3db31cb700428b16cd8e51"}
       :type 1
       :symbol "STT"
       :amount "1.0000000000000000"
       :decimals 18}]}]
   ["6872cfb4-9122-4d39-b58c-5dff441f94d4"
    {:id "6872cfb4-9122-4d39-b58c-5dff441f94d4"
     :type 6
     :token_criteria
     [{:contract_addresses
       {:420 "0x98C28243BeFD84f5B83BA49fE843e6Cbd9a55AD4"}
       :type 2
       :symbol "OWNPER"
       :name "Owner-Permission Drawer"
       :amount "1"}]
     :is_private true}]
   ["730c3fd3-3fcf-4d48-934a-b175b571c5fe"
    {:id "730c3fd3-3fcf-4d48-934a-b175b571c5fe"
     :type 2
     :token_criteria
     [{:contract_addresses
       {:5        "0x0000000000000000000000000000000000000000"
        :420      "0x0000000000000000000000000000000000000000"
        :421613   "0x0000000000000000000000000000000000000000"
        :421614   "0x0000000000000000000000000000000000000000"
        :11155111 "0x0000000000000000000000000000000000000000"}
       :type 1
       :symbol "ETH"
       :amount "1.0000000000000000"
       :decimals 18}]}]
   ["75a6c876-7be4-4830-a82b-b672986329da"
    {:id "75a6c876-7be4-4830-a82b-b672986329da"
     :type 5
     :token_criteria
     [{:contract_addresses
       {:420 "0x2334ab535D0146d4a78C24894Cdc0C466cF3159e"}
       :type 2
       :symbol "TMPER"
       :name "TMaster-Permission Drawer"
       :amount "1"}]
     :is_private true}]])

(defn view
  []
  (let [{id :community-id} (rf/sub [:get-screen-params])
        {:keys [highest-permission-role tokens can-request-access?]}
        (rf/sub [:community/token-gated-overview id])
        highest-role-text
        (i18n/label
         (communities.utils/role->translation-key highest-permission-role :t/member))
        selected-addresses (rf/sub [:communities/selected-permission-addresses id])]

    (tap> (rf/sub [:community/token-gated-overview id]))
    (tap> ["app-db" @re-frame.db/app-db])
    (tap> ["id" id])
    (tap> ["tokens" tokens])

    [rn/view {:style style/container}
     (when (and highest-permission-role (seq selected-addresses))
       [rn/view
        {:style style/highest-role}
        [rn/view {:style {:flex-direction :column}}
         [quo/text {:weight :medium}
          (i18n/label :t/you-eligible-to-join-as {:role highest-role-text})]
         [quo/text {:style {:padding-bottom 18} :size :paragraph-2}
          (if can-request-access?
            (i18n/label :t/you-hodl)
            (i18n/label :t/you-must-hold))]
         [rn/view {:style {:align-items :flex-start}}
          [quo/collectible-tag
           {:size                :size-24
            :collectible-name    "Collectible"
            :collectible-id      "#123"
            :options             :hold
            :collectible-img-src (resources/mock-images :collectible)}]]]])
     [rn/view
      {:style style/highest-role}
      [rn/view {:style {:flex-direction :column :margin-top 12}}
       [quo/text {:weight :medium}
        (i18n/label :t/you-eligible-to-join-as {:role "Member"})]
       [quo/text {:style {:padding-bottom 18} :size :paragraph-2}
        (if can-request-access?
          (i18n/label :t/you-hodl)
          (i18n/label :t/you-must-hold))]
       [quo/token-requirement-list {:tokens mock-token}]]]]))
