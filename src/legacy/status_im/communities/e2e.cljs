(ns legacy.status-im.communities.e2e
  (:require [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

;;NOTE: ONLY FOR QA

(rf/defn create-closed-community
  {:events [:fast-create-community/create-closed-community]}
  [_]
  {:json-rpc/call [{:method      "wakuext_createClosedCommunity"
                    :params      []
                    :js-response true
                    :on-success  #(rf/dispatch [:sanitize-messages-and-process-response %])
                    :on-error    #(log/error "failed to create closed community." {:error %})}]
   :dispatch      [:hide-bottom-sheet]})

(rf/defn create-open-community
  {:events [:fast-create-community/create-open-community]}
  [_]
  {:json-rpc/call [{:method      "wakuext_createOpenCommunity"
                    :params      []
                    :js-response true
                    :on-success  #(rf/dispatch [:sanitize-messages-and-process-response %])
                    :on-error    #(log/error "failed to create open community." {:error %})}]
   :dispatch      [:hide-bottom-sheet]})

(rf/defn create-token-gated-community
  {:events [:fast-create-community/create-token-gated-community]}
  [_]
  {:json-rpc/call [{:method      "wakuext_createTokenGatedCommunity"
                    :params      []
                    :js-response true
                    :on-success  #(rf/dispatch [:sanitize-messages-and-process-response %])
                    :on-error    #(log/error "failed to create token gated community." {:error %})}]
   :dispatch      [:hide-bottom-sheet]})
