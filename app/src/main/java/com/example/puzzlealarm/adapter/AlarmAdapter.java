package com.example.puzzlealarm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puzzlealarm.R;
import com.example.puzzlealarm.model.Alarm;
import com.example.puzzlealarm.model.PuzzleType;

import java.util.List;
import java.util.Locale;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    public interface OnTapListener{
        void onClick(Alarm alarm); //короткое нажатие (редактировать)
        void onHold(Alarm alarm); //длинное нажатие (удалить)
    }

    private List<Alarm> alarms;

    private final OnTapListener listener;

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm alarm = alarms.get(position);
        holder.time.setText(String.format(Locale.getDefault(), "%02d:%02d", alarm.getHour(),
        alarm.getMin())); // например 08:32

        String puzzleName = "?";

        if (alarm.getPuzzleType() == PuzzleType.MATHEMATICS){
            puzzleName = "Математика";
        } else if (alarm.getPuzzleType() == PuzzleType.ORDER_PUZZLE) {
            puzzleName = "Цифры от 1 до 16";
        }

        holder.textPuzzle.setText(puzzleName);

        holder.textEnabled.setText(alarm.isOn() ? "Вкл" : "Выкл");
        holder.textEnabled.setTextColor(alarm.isOn() ? 0xFF008000 : 0xFFff0000);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(alarm);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onHold(alarm);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return alarms == null ? 0 : alarms.size();
    }

    public AlarmAdapter(List<Alarm> alarms, OnTapListener listener) {
        this.alarms = alarms;
        this.listener = listener;
    }

    /**
     * Метод для обновления списка будильников
     * Вызывает notifyDataSetChanged()
     */
    public void setAlarms(List<Alarm> alarms){
        this.alarms = alarms;
        notifyDataSetChanged();
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView time, textPuzzle, textEnabled;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            textPuzzle = itemView.findViewById(R.id.text_puzzle);
            textEnabled = itemView.findViewById(R.id.text_enabled);
        }
    }
}
