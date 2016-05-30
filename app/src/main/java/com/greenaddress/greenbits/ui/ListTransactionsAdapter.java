package com.greenaddress.greenbits.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.greenaddress.greenbits.GaService;

import org.bitcoinj.core.Coin;
import org.bitcoinj.utils.MonetaryFormat;

import java.text.DecimalFormat;
import java.util.List;

public class ListTransactionsAdapter extends
        RecyclerView.Adapter<ListTransactionsAdapter.ViewHolder> {

    private final static int REQUEST_TX_DETAILS = 4;

    private final List<TransactionItem> transactions;
    private final String btcUnit;
    private final Activity context;
    private final GaService mService;

    public ListTransactionsAdapter(final Activity context, final GaService service,
                                   final List<TransactionItem> transactions) {
        this.transactions = transactions;
        this.btcUnit = (String) service.getUserConfig("unit");
        this.context = context;
        mService = service;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_element_transaction, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final TransactionItem txItem = transactions.get(position);


        final long val = txItem.amount;
        final Coin coin = Coin.valueOf(val);
        final MonetaryFormat bitcoinFormat = CurrencyMapper.mapBtcUnitToFormat(btcUnit);
        holder.bitcoinScale.setText(Html.fromHtml(CurrencyMapper.mapBtcUnitToPrefix(btcUnit)));
        if (btcUnit == null || btcUnit.equals("bits")) {
            holder.bitcoinIcon.setText("");
            holder.bitcoinScale.setText("bits ");
        } else {
            holder.bitcoinIcon.setText(Html.fromHtml("&#xf15a; "));
        }

        final String btcBalance = bitcoinFormat.noCode().format(coin).toString();

        final DecimalFormat formatter = new DecimalFormat("#,###.########");
        try {
            holder.textValue.setText(formatter.format(Double.valueOf(btcBalance)));
        } catch (@NonNull final NumberFormatException e) {
            holder.textValue.setText(btcBalance);
        }

        if (!mService.isSPVEnabled() ||
            txItem.spvVerified || txItem.isSpent || txItem.type.equals(TransactionItem.TYPE.OUT)) {
            holder.textValueQuestionMark.setVisibility(View.GONE);
        } else {
            holder.textValueQuestionMark.setVisibility(View.VISIBLE);
        }

        final Resources res = context.getResources();

        if (txItem.doubleSpentBy == null) {
            holder.textWhen.setTextColor(res.getColor(R.color.tertiaryTextColor));
            holder.textWhen.setText(TimeAgo.fromNow(txItem.date.getTime(), context));
        } else {
            switch (txItem.doubleSpentBy) {
                case "malleability":
                    holder.textWhen.setTextColor(Color.parseColor("#FF8000"));
                    holder.textWhen.setText(context.getResources().getText(R.string.malleated));
                    break;
                case "update":
                    holder.textWhen.setTextColor(Color.parseColor("#FF8000"));
                    holder.textWhen.setText(context.getResources().getText(R.string.updated));
                    break;
                default:
                    holder.textWhen.setTextColor(Color.RED);
                    holder.textWhen.setText(context.getResources().getText(R.string.doubleSpend));
            }
        }

        if (!txItem.replaceable) {
            holder.textReplaceable.setVisibility(View.GONE);
        } else {
            holder.textReplaceable.setVisibility(View.VISIBLE);
        }

        final boolean humanCpty = txItem.type.equals(TransactionItem.TYPE.OUT)
                && txItem.counterparty != null && txItem.counterparty.length() > 0
                && !GaService.isValidAddress(txItem.counterparty);

        final String message = txItem.memo == null || txItem.memo.isEmpty() ?
                humanCpty ?
                        txItem.counterparty
                        :
                        getTypeString(txItem.type)
                :
                humanCpty ?
                        String.format("%s %s", txItem.counterparty, txItem.memo)
                        :
                        txItem.memo;


        holder.textWho.setText(message);

        holder.mainLayout.setBackgroundColor(val > 0 ?
                res.getColor(R.color.superLightGreen) :
                res.getColor(R.color.superLightPink)
        );

        if (txItem.hasEnoughConfirmations()) {
            holder.inOutIcon.setText(val > 0 ?
                    Html.fromHtml("&#xf090;") :
                    Html.fromHtml("&#xf08b;")
            );
            holder.listNumberConfirmation.setVisibility(View.GONE);
        } else {
            holder.inOutIcon.setText(Html.fromHtml("&#xf017;"));
            holder.listNumberConfirmation.setVisibility(View.VISIBLE);
            holder.listNumberConfirmation.setText(String.valueOf(txItem.getConfirmations()));
        }

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Intent transactionActivity = new Intent(context, TransactionActivity.class);
                transactionActivity.putExtra("TRANSACTION", txItem);
                context.startActivityForResult(transactionActivity, REQUEST_TX_DETAILS);
            }
        });
    }

    private String getTypeString(@NonNull final TransactionItem.TYPE type) {
        switch (type) {
            case IN:
                return context.getString(R.string.txTypeIn);
            case OUT:
                return context.getString(R.string.txTypeOut);
            case REDEPOSIT:
                return context.getString(R.string.txTypeRedeposit);
            default:
                return "No type";
        }
    }

    @Override
    public int getItemCount() {
        return transactions == null ? 0 : transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView listNumberConfirmation;
        public final TextView textValue;
        public final TextView textWhen;
        public final TextView textReplaceable;
        public final TextView bitcoinIcon;
        public final TextView textWho;
        public final TextView inOutIcon;
        public final TextView bitcoinScale;
        public final TextView textValueQuestionMark;
        public final RelativeLayout mainLayout;

        public ViewHolder(final View itemView) {

            super(itemView);

            textValue = (TextView) itemView.findViewById(R.id.listValueText);
            textValueQuestionMark = (TextView) itemView.findViewById(R.id.listValueQuestionMark);
            textWhen = (TextView) itemView.findViewById(R.id.listWhenText);
            textReplaceable = (TextView) itemView.findViewById(R.id.listReplaceableText);
            textWho = (TextView) itemView.findViewById(R.id.listWhoText);
            inOutIcon = (TextView) itemView.findViewById(R.id.listInOutIcon);
            mainLayout = (RelativeLayout) itemView.findViewById(R.id.list_item_layout);
            bitcoinIcon = (TextView) itemView.findViewById(R.id.listBitcoinIcon);
            bitcoinScale = (TextView) itemView.findViewById(R.id.listBitcoinScaleText);
            listNumberConfirmation = (TextView) itemView.findViewById(R.id.listNumberConfirmation);
        }
    }
}
