package com.wh.gaea.interfaces;

public enum ChangeType {
	ctAdd, ctMove, ctResize, ctPaste, ctAddLink, ctRemoveLink, ctRemove, ctSelected, ctDeselected, ctSelecteds,
	ctMouseRelease, ctKeyUp, ctLineChanged, ctBackspacing, ctBringToTop, ctSendToBack, ctBackEdited,
	ctReportApplyTemplate, ctReportRemoveRow, ctReportRemoveCell, ctReportRemoveColumn, ctReportAddRow,
	ctReportAddColumn, ctReportMerge, ctReportChangeCell, ctReportChangeRowCount, ctReportChangeColCount,
	ctReportChangeRowSize, ctReportChangeColSize, ctReportResetScale, ctReportResetCellSize, ctReportResetSize,
	ctReportSplit, ctReportSelected, ctReportDeselected
}