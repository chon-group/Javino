/*
  Javino.h - Library communication for Arduino and Jason.
Version Stable 1.2
  Created by Lazarin, NM and Pantoja, CE - January 29, 2015.
    nilson.lazarin@cefet-rj.br
	carlos.pantoja@cefet-rj.br

  Updated in 2022-06-18
  Released into the public domain.

*/

#ifndef Javino_h
#define Javino_h
#define sizeofarraymsg 261

#include "Arduino.h"

class Javino
{
  public:
    Javino();
    void sendmsg(String msg);
    String getmsg();
    boolean availablemsg();
  private:
	int _x;
	int _d;
	int _n;
	char _arraymsg[sizeofarraymsg];
	boolean _msg;
	String _finalymsg;
	String int2hex(int v);
	void inicializa();
	void listening();
	void timeout();
	void registra();
	void monitormsg();
	void aborta();
	int sizeofmsg();
	void tratamsg();
	boolean preambulo();
	int hex2int(char z[]);
	int forInt(char v);
	String char2string(char in[], int sizein);
};

#endif
